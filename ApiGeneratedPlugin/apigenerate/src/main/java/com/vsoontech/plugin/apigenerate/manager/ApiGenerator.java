package com.vsoontech.plugin.apigenerate.manager;


import com.vsoontech.plugin.apigenerate.ApiProperties;
import com.vsoontech.plugin.apigenerate.entity.ApiDetail;
import com.vsoontech.plugin.apigenerate.utils.Logc;

public class ApiGenerator {

    private String appPackageName;
    private String buildConfigOutputDir;
    private String projectDir;

    public ApiGenerator(String appPackageName, String buildConfigSrcOutputDir, String projectDir) {
        this.appPackageName = appPackageName;
        this.buildConfigOutputDir = buildConfigSrcOutputDir;
        this.projectDir = projectDir;
    }

    public void generate() {
        if (!isEmpty(appPackageName)
            && !isEmpty(buildConfigOutputDir)
            && !isEmpty(projectDir)) {
            Logc.d(appPackageName);
            Logc.d(buildConfigOutputDir);
            Logc.d(projectDir);
            Logc.d("[ Api Remote Verify ! ]");
            ApiServiceManager serviceManager = new ApiServiceManager(projectDir);
            serviceManager.connect(apiDetailList -> {
                Logc.d("[ Class Parse/Convert/Simulation ! ]");
                new ClassParseManager().run(apiDetailList, apiDetails -> {
                    if (apiDetails != null
                        && !apiDetails.isEmpty()) {
                        ClassConvertManager classConvertManager = new ClassConvertManager(buildConfigOutputDir,
                            appPackageName);
                        SimulationManager simulationManager = new SimulationManager(projectDir);
                        for (ApiDetail apiDetail : apiDetails) {
                            try {
                                classConvertManager.generate(apiDetail);
                            } catch (Exception e) {
                                Logc.d(apiDetail.desc + " generate java file error !");
                            }
                            try {
                                simulationManager.generate(apiDetail);
                            } catch (Exception e) {
                                Logc.d(apiDetail.desc + " generate simulation data error !");
                            }
                        }
                    }
                });

            });

//            try{
//                BuildConfigGenerator generator = new BuildConfigGenerator(
//                    new File("D:\\Android\\workspace\\ApiGeneratedPlugin\\app\\build\\generated\\api\\out"),
//                    "com.vsoontech.plugin.genss");
//                generator.generate();
//            }catch (Exception e){
//                e.printStackTrace();
//            }
        }

    }

    private boolean isEmpty(CharSequence s) {
        if (s == null) {
            return true;
        } else {
            return s.length() == 0;
        }
    }
}
