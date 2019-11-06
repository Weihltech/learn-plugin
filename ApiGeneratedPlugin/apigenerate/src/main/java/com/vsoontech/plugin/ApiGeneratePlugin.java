package com.vsoontech.plugin;

import com.android.build.gradle.AppPlugin;
import com.android.build.gradle.internal.VariantManager;
import com.android.build.gradle.internal.crash.CrashReporting;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.tasks.GenerateBuildConfig;
import com.vsoontech.plugin.apigenerate.ApiConfig;
import com.vsoontech.plugin.apigenerate.manager.ApiGenerator;
import com.vsoontech.plugin.apigenerate.utils.Logc;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.api.tasks.TaskProvider;

public class ApiGeneratePlugin implements Plugin<Project> {


    @Override
    public void apply(Project project) {
        ApiConfig.init(project);
        if (Logc.openLog()) {
            Logc.d("preRun !");
        }

        // 创建Task
        HashMap<String, String> opMap = new HashMap<>();
        opMap.put("group", "apiGenerate");
        TaskContainer taskContainer = project.getTasks();
        opMap.put("name", "logConfig");
        taskContainer.create(opMap).doLast(
            task -> {
                // 打印相关配置信息，用于核对参数
                ApiConfig.print();
            }
        );
        opMap.put("name", "run");
        Action<Task> generateApiTask = task -> {
            // 必须： ApiConfig.init(project); 之后
            new ApiGenerator().run();
        };
        taskContainer.create(opMap).doFirst(generateApiTask);

        // 项目 action by [reBuild ,switch Variants] , etc.
//        Task preBuildTask = taskContainer.findByName("preBuild");
//        if (preBuildTask != null) {
//            preBuildTask.doLast(task -> {
//                try {
//                    File apiGenDir = new File(task.getProject().getBuildDir() + "\\generated\\api");
//                    FileUtils.cleanDirectory(apiGenDir);
//                    Logc.d("[Clean ApiGeneratedDir ].");
//                } catch (Exception e) {
//                    Logc.e("[Clean ApiGeneratedDir ]." + e.getMessage());
//                }
//            });
//        }

        // 项目 action by  [BuildConfigTask]

        attachedToBuildConfigTask(project, generateApiTask);

    }

    private void attachedToBuildConfigTask(Project project, Action<Task> generateApiTask) {
        project.afterEvaluate(project1 -> CrashReporting.runAction(
            () -> {
                try {
                    AppPlugin appPlugin = project.getPlugins().findPlugin(AppPlugin.class);
                    if (appPlugin != null) {
                        VariantManager variantManager = appPlugin.getVariantManager();
                        List<VariantScope> scopeList = variantManager.getVariantScopes();
                        for (VariantScope scope : scopeList) {
                            if (scope.getFullVariantName().endsWith("ebug")
                                || scope.getFullVariantName().endsWith("elease")) {
                                TaskProvider taskProvider = scope.getTaskContainer().getGenerateBuildConfigTask();
                                if (taskProvider != null) {
                                    ((Task) taskProvider.get()).doLast(generateApiTask);
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }));
    }


}
