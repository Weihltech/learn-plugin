package com.vsoontech.plugin.apigenerate;

import com.android.annotations.NonNull;
import com.android.build.gradle.internal.core.GradleVariantConfiguration;
import com.android.build.gradle.internal.scope.VariantScope;
import com.android.build.gradle.internal.tasks.NonIncrementalTask;
import com.android.build.gradle.internal.tasks.TaskInputHelper;
import com.android.build.gradle.internal.tasks.factory.VariantTaskCreationAction;
import com.android.build.gradle.internal.variant.BaseVariantData;
import com.vsoontech.plugin.apigenerate.manager.ApiGenerator;
import com.vsoontech.plugin.apigenerate.utils.Logc;

public class ApiGenerateTask extends NonIncrementalTask {

    private String appPackageName;
    private String buildConfigSrcOutputDir;
    private String projectDir;

    @Override
    protected void doTaskAction() throws Exception {
        Logc.d("ApiGenerateTask ! ");
        new ApiGenerator(appPackageName,
            buildConfigSrcOutputDir,
            projectDir).generate();
    }

    public static final class CreationAction
        extends VariantTaskCreationAction<ApiGenerateTask> {

        public CreationAction(@NonNull VariantScope scope) {
            super(scope);
        }

        @Override
        @NonNull
        public String getName() {
            return getVariantScope().getTaskName("generate", "VsoonApi");
        }

        @Override
        @NonNull
        public Class<ApiGenerateTask> getType() {
            return ApiGenerateTask.class;
        }

//        @Override
//        public void handleProvider(
//            @NonNull TaskProvider<? extends ApiGenerateTask> taskProvider) {
//            super.handleProvider(taskProvider);
//            Logc.d("ApiGenerateTask handleProvider !" + taskProvider.getName());
////            getVariantScope().getTaskContainer().setGenerateBuildConfigTask(taskProvider);
//        }

        @Override
        public void configure(@NonNull ApiGenerateTask task) {
            super.configure(task);
            VariantScope scope = getVariantScope();

            BaseVariantData variantData = scope.getVariantData();

            final GradleVariantConfiguration variantConfiguration =
                variantData.getVariantConfiguration();

            task.appPackageName = TaskInputHelper.memoize(variantConfiguration::getApplicationId).get();
            // source/buildConfig
            task.buildConfigSrcOutputDir = scope.getBuildConfigSourceOutputDir().getAbsolutePath();
            task.projectDir = scope.getGlobalScope().getProject().getProjectDir().getAbsolutePath();

            // source/dataBinding
//             task.buildConfigSrcOutputDir = scope.getClassOutputForDataBinding().getAbsolutePath();

            // app/build/intermediates/res
//             task.buildConfigSrcOutputDir = scope.getCompiledResourcesOutputDir().getAbsolutePath();

//            Logc.d("ApiGenerateTask buildConfigPackageName = " + TaskInputHelper
//                .memoize(variantConfiguration::getOriginalApplicationId));
//
//            Logc.d("ApiGenerateTask appPackageName = " + TaskInputHelper
//                .memoize(variantConfiguration::getApplicationId));
//
//            Logc.d("ApiGenerateTask flavorName = " + TaskInputHelper
//                .memoize(variantConfiguration::getFlavorName));
//
//            Logc.d("ApiGenerateTask flavorNamesWithDimensionNames = " + TaskInputHelper
//                .memoize(variantConfiguration::getFlavorNamesWithDimensionNames));

//            Logc.d("ApiGenerateTask SourceOutputDir = " + scope.getBuildConfigSourceOutputDir());

        }
    }
}
