package com.vsoontech.plugin;

import com.android.annotations.NonNull;
import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.crash.CrashReporting;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.TaskInputHelper;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.vsoontech.plugin.apigenerate.ApiProperties;
import com.vsoontech.plugin.apigenerate.Config;
import com.vsoontech.plugin.apigenerate.manager.ApiGenerator;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.util.HashMap;
import java.util.List;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

public class ApiGeneratePlugin implements Plugin<Project> {

    HashMap<String, String> opMap = new HashMap<>();

    @Override
    public void apply(Project project) {
        opMap.put("group", "apiGenerate");

        new ApiProperties(project).read();

        opMap.put("name", "logConfig");
        project.getTasks().create(opMap).doLast(
            task -> Logc.d("Config : apiId = " + Config.apiProject
                + " ; apiVersion = " + Config.apiVersion
                + " ; apiOutSrc = " + Config.genOutSrc
                + " ; genOutSrcPath = " + Config.genOutSrcPath));

        androidPlugin(project);
    }


    private void androidPlugin(Project project) {
        project.afterEvaluate(project1 -> CrashReporting.runAction(
            () -> {
                AppPlugin appPlugin = project.getPlugins().findPlugin(AppPlugin.class);
                if (appPlugin != null) {
                    VariantManager variantManager = appPlugin.getVariantManager();
                    List<VariantScope> scopeList = variantManager.getVariantScopes();
                    for (VariantScope scope : scopeList) {
                        if ("debug".equals(scope.getFullVariantName())
                            || "release".equals(scope.getFullVariantName())) {
                            createApiGenerateTask(project, scope);
                        }
                    }
                }
            }));
    }

    private void createApiGenerateTask(Project mProject, @NonNull VariantScope scope) {
//        TaskFactoryImpl taskFactory = new TaskFactoryImpl(mProject.getTasks());
//        TaskProvider<ApiGenerateTask> apiGenerateTaskTaskProvider =
//            taskFactory.register(new ApiGenerateTask.CreationAction(scope));
//
//        ApiGenerateTask apiGenTask = apiGenerateTaskTaskProvider.get();
//        TaskProvider<? extends GenerateBuildConfig> configTaskProvider = scope.getTaskContainer()
//            .getGenerateBuildConfigTask();
//        if (configTaskProvider != null) {
//            Task configTask = configTaskProvider.get();
//            configTask.doLast(task -> apiGenTask.taskAction());
//        }

        GradleVariantConfiguration variantConfiguration = scope.getVariantData().getVariantConfiguration();
        String appPackageName = TaskInputHelper.memoize(variantConfiguration::getApplicationId).get();
        String buildConfigSrcOutputDir = scope.getBuildConfigSourceOutputDir().getAbsolutePath();
        String projectDir = scope.getGlobalScope().getProject().getProjectDir().getAbsolutePath();

        TaskContainer tTaskContainer = mProject.getTasks();
        opMap.put("name", "generate" + scope.getFullVariantName());
        Action<Task> generateApiTask = task -> {
            new ApiGenerator(appPackageName,
                buildConfigSrcOutputDir,
                projectDir).generate();
        };

        tTaskContainer.create(opMap).doLast(generateApiTask);

        TaskProvider<? extends GenerateBuildConfig> configTaskProvider = scope.getTaskContainer()
            .getGenerateBuildConfigTask();
        if (configTaskProvider != null) {
            Task configTask = configTaskProvider.get();
            configTask.doFirst(task -> {
                Config.genOutSrcPath = "app\\build\\generated\\source\\buildConfig\\" + scope.getFullVariantName();
                new ApiProperties(mProject).save();
            });
            configTask.doLast(generateApiTask);
        }

    }
}
