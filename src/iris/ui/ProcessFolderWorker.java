/**
 * 
 */
package iris.ui;

import java.awt.Toolkit;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import javax.swing.SwingWorker;

import iris.profiles.BasicProfile;
import iris.profiles.BasicProfileInverted;
import iris.profiles.BasicProfileNoEmptyCheck;
import iris.profiles.BsubtilisHazyProfileHSB;
import iris.profiles.BsubtilisSporulationProfile;
import iris.profiles.CPRGProfile384_ourCamera2;
import iris.profiles.ColonyOpacityProfile;
import iris.profiles.ColonyOpacityProfileInverted;
import iris.profiles.ColorProfileEcoli;
import iris.profiles.ColorProfileEcoliNaturalIsolates;
import iris.profiles.ColorProfilePA;
import iris.profiles.ColorProfile_SimpleSegmentation;
import iris.profiles.EcoliGrowthProfile;
import iris.profiles.EcoliOpacityProfile384;
import iris.profiles.EcoliOpacityProfile384_HazyColonies;
import iris.profiles.EcoliOpacityProfile96;
import iris.profiles.MorphologyProfileCandida96;
import iris.profiles.MorphologyProfilePA384;
import iris.profiles.MorphologyProfilePA96;
import iris.profiles.MorphologyProfileStm96;
import iris.profiles.OpacityProfile;
import iris.profiles.OpacityProfile2;
import iris.profiles.XgalProfile;

/**
 * @author George Kritikos
 *
 */
//public class ProcessFolderWorker extends SimpleSwingWorker {
public class ProcessFolderWorker extends SwingWorker<String, String> {

	//public IrisGUI parentJFrame;
	File directory;




	//	public ProcessFolderWorker(IrisGUI parent){
	//		parentJFrame = parent;
	//	}


	/**
	 * This function is executed when the open folder button is clicked
	 * @return 
	 */
	@Override
	protected String doInBackground() throws Exception {

		//open the log file for writing
		IrisFrontend.openLog(directory.getAbsolutePath());
		IrisFrontend.writeToLog("--- Iris version " + IrisFrontend.IrisVersion + " log file\tbuild "+IrisFrontend.IrisBuild+" ---\n");
		IrisFrontend.writeToLog("-- Started processing files at "+ new Date() + " --\n");

		//write user settings
		try{
			IrisFrontend.writeToLog("-- Loaded user settings: -- \n\n");
			String oneProfileUserSettings = IrisFrontend.userSettings.getProfileSettingsAsString(IrisFrontend.selectedProfile);
			IrisFrontend.writeToLog(oneProfileUserSettings);
		}catch(Exception e){
			IrisFrontend.writeToLog("-- error writing user settings, check file iris.user.settings.json -- \n\n");
		}
		IrisFrontend.writeToLog("\n\n-----------------------------------------\n\n\n");
		

		//get a list of the files in the directory, keeping only image files
		File[] filesInDirectory = directory.listFiles(new PicturesFilenameFilter());

		int i=0;
		int max = filesInDirectory.length;

		for (File file : filesInDirectory) {

			if(!file.exists())
				continue;

			if(file.isDirectory())
				continue;

			try{
				processSingleFile(file);
			}
			catch(Exception e){
				System.out.println("Error processing file!\n");
				e.printStackTrace(System.err);
			}

			i++;
			int progress = Math.min(i*100/max, 100);
			setProgress(progress);
			System.out.println(i + " / " + max + "\t(" + progress +"% done)" +  "\n\n");

			publish("...done! " + "\n\n\n");
		}

		//IrisFrontend.closeLog();
		//close the log file
		IrisFrontend.writeToLog("\n\n-----------------------------------------\n");
		IrisFrontend.writeToLog("-- Done processing all files at "+ new Date() + " --\n");
		IrisFrontend.closeLog();

		return(null);
	}


	public static void processSingleFile(File file){


		String filename = file.getAbsolutePath();

		//if we set it here, then it will be called both on GUI or console s/w invocation
		if(IrisFrontend.singleColonyRun==true){
			IrisFrontend.settings.numberOfRowsOfColonies = 1;
			IrisFrontend.settings.numberOfColumnsOfColonies = 1;

			if(filename.contains("colony_")){
				IrisFrontend.settings.userDefinedRoi=true;
			}

		}

		//publish("Now processing file " + "\n");
		//System.out.println("Now processing file " + "\n");


		File irisFile = new File(filename+".iris");
		File irisFileDummy = new File(filename+".iris.dummy");


		if(IrisFrontend.nice){	
			//if the iris file exists, or if another iris instance started working on it
			if(irisFile.exists() || irisFileDummy.exists()){ 
				String justFilename = irisFile.getName();
				System.out.println("\n\nIris file already exists:\n  "+justFilename);
				return;
			}
			//if it didn't write down a dummy file, so that other instances won't start working on the same
			else{
				try {
					irisFileDummy.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}

		/**
		 * Decide which profile to use, according to the profile name
		 */
		String profileName = IrisFrontend.selectedProfile;

		if(profileName.equals("Stm growth")){
			BasicProfile basicProfile = new BasicProfile();
			basicProfile.analyzePicture(filename);
		}

		if(profileName.equals("Ecoli growth -- no empty check")){
			BasicProfileNoEmptyCheck basicProfileNoEmptyCheck = new BasicProfileNoEmptyCheck();
			basicProfileNoEmptyCheck.analyzePicture(filename);
		}

		else if(profileName.equals("Ecoli opacity 1536")){
			ColonyOpacityProfile colonyOpacityProfile = new ColonyOpacityProfile();
			colonyOpacityProfile.analyzePicture(filename);			
		}			

		else if(profileName.equals("B.subtilis Opacity (HSB)")){
			BsubtilisHazyProfileHSB bsubtilisHazyProfileHSB = new BsubtilisHazyProfileHSB();
			bsubtilisHazyProfileHSB.analyzePicture(filename);			
		}

		else if(profileName.equals("B.subtilis sporulation")){
			BsubtilisSporulationProfile bsubtilisSporulationProfile = new BsubtilisSporulationProfile();
			bsubtilisSporulationProfile.analyzePicture(filename);			
		}

		else if(profileName.equals("Ecoli growth")){
			EcoliGrowthProfile ecoliGrowthProfile = new EcoliGrowthProfile();
			ecoliGrowthProfile.analyzePicture(filename);			
		}

		else if(profileName.equals("Ecoli opacity 384")){
			EcoliOpacityProfile384 ecoliOpacityProfile384 = new EcoliOpacityProfile384();
			ecoliOpacityProfile384.analyzePicture(filename);			
		}

		else if(profileName.equals("Ecoli opacity 384 - hazy colonies")){
			EcoliOpacityProfile384_HazyColonies ecoliOpacity384_hazy = new EcoliOpacityProfile384_HazyColonies();
			ecoliOpacity384_hazy.analyzePicture(filename);			
		}

		else if(profileName.equals("Ecoli opacity 96")){
			EcoliOpacityProfile96 ecoliOpacity96 = new EcoliOpacityProfile96();
			ecoliOpacity96.analyzePicture(filename);
		}

		else if(profileName.equals("Colony growth")){
			ColonyOpacityProfile colonyOpacity = new ColonyOpacityProfile();
			colonyOpacity.analyzePicture(filename);
		}

		else if(profileName.contains("Xgal assay")){
			XgalProfile xgalProfile = new XgalProfile();
			xgalProfile.analyzePicture(filename);			
		}

		else if(profileName.equals("CPRG 384")){
			CPRGProfile384_ourCamera2 cprgProfile384_ourCamera2 = new CPRGProfile384_ourCamera2();
			cprgProfile384_ourCamera2.analyzePicture(filename);
		}

		else if(profileName.equals("CPRG profile")){
			CPRGProfile384_ourCamera2 cprgProfile384_ourCamera2 = new CPRGProfile384_ourCamera2();
			cprgProfile384_ourCamera2.analyzePicture(filename);
		}

		//		else if(profileName.equals("Biofilm formation")){
		//			ColorProfile colorProfile = new ColorProfile();
		//			colorProfile.analyzePicture(filename);
		//		}

		else if(profileName.equals("Biofilm formation")){
			ColorProfileEcoli colorProfileEcoli = new ColorProfileEcoli();
			colorProfileEcoli.analyzePicture(filename);
		}

		else if(profileName.equals("Biofilm formation PA")){
			ColorProfilePA colorProfilePA = new ColorProfilePA();
			colorProfilePA.analyzePicture(filename);
		}

		else if(profileName.equals("Biofilm formation Ecoli")){
			ColorProfileEcoli colorProfileEcoli = new ColorProfileEcoli();
			colorProfileEcoli.analyzePicture(filename);
		}

		else if(profileName.equals("Biofilm formation Ecoli Natural Isolates")){
			ColorProfileEcoliNaturalIsolates colorProfileEcoliNaturalIsolates = new ColorProfileEcoliNaturalIsolates();
			colorProfileEcoliNaturalIsolates.analyzePicture(filename);
		}

		else if(profileName.equals("Biofilm formation - Simple Grid")){
			ColorProfile_SimpleSegmentation colorProfile_simpleSegmentation = new ColorProfile_SimpleSegmentation();
			colorProfile_simpleSegmentation.analyzePicture(filename);
		}

		else if(profileName.equals("Opacity")){
			OpacityProfile opacityProfile = new OpacityProfile();
			opacityProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Opacity (fixed grid)")){
			OpacityProfile2 opacityProfile2 = new OpacityProfile2();
			opacityProfile2.analyzePicture(filename);
		}

		else if(profileName.equals("Morphology Profile [Candida 96-plates]")){
			MorphologyProfileCandida96 morphologyProfile = new MorphologyProfileCandida96();
			morphologyProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Morphology profile")){
			MorphologyProfileCandida96 morphologyProfile = new MorphologyProfileCandida96();
			morphologyProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Morphology Profile [Pseudomonas 96-plates]")){
			MorphologyProfilePA96 morphologyProfile = new MorphologyProfilePA96();
			morphologyProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Morphology Profile [Pseudomonas 384-plates]")){
			MorphologyProfilePA384 morphologyProfile = new MorphologyProfilePA384();
			morphologyProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Morphology&Color profile")){//pseudomonas colonies but it'll work for more than that
			MorphologyProfilePA384 morphologyProfile = new MorphologyProfilePA384();
			morphologyProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Morphology Profile [Salmonella 96-plates]")){
			MorphologyProfileStm96 morphologyProfile = new MorphologyProfileStm96();
			morphologyProfile.analyzePicture(filename);
		}

		else if(profileName.equals("Growth profile inverted")){
			BasicProfileInverted profile = new BasicProfileInverted();
			profile.analyzePicture(filename);
		}

		else if(profileName.equals("Colony growth inverted")){
			ColonyOpacityProfileInverted profile = new ColonyOpacityProfileInverted();
			profile.analyzePicture(filename);
		}




		else{
			System.err.println("Unknown profile name: \"" + profileName +"\"");
			return;
		}


		//we need to clean up, by removing any dummy files
		if(IrisFrontend.nice){
			irisFileDummy.delete();
		}


	}


	/**
	 * This method will wait for all threads to finish execution before carrying on
	 */
	private static void waitForThreads(List<Callable<Object>> todoIndex_) {
		List<Future<Object>> listOfFutures = null;
		try {
			listOfFutures = IrisFrontend.executorService.invokeAll(todoIndex_);

			for (Future<Object> future : listOfFutures) {
				future.get();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		//wait for all futures to finish executing
		IrisFrontend.executorService.shutdown();

	}


	protected void process(String item) {
		System.out.println(item);
	}

	/*
	 * Executed in event dispatching thread
	 */
	@Override
	public void done() {

		// call get to make sure any exceptions 
		// thrown during doInBackground() are 
		// thrown again
		try {
			get();
		} catch (final InterruptedException ex) {
			throw new RuntimeException(ex);
		} catch (final ExecutionException ex) {
			throw new RuntimeException(ex.getCause());
		}

		Toolkit.getDefaultToolkit().beep();
		IrisGUI.btnOpenFolder.setEnabled(true);

		System.out.println("\n\n\n\nDone processing all files\n\n");

		//close the log file
		IrisFrontend.writeToLog("\n\n-----------------------------------------\n");
		IrisFrontend.writeToLog("-- Done processing all files at "+ new Date() + " --\n");
		IrisFrontend.closeLog();

	}


	/**
	 * This function gets a value i and it's maximum possible value
	 * and calculates what percentage of the max value is the given value i
	 * @param i
	 * @param max
	 * @return
	 */
	private int getPercentageDone(int i, int max){
		float fraction = i/max;
		float percentage = fraction*100;

		return(Math.round(percentage));
	}

}
