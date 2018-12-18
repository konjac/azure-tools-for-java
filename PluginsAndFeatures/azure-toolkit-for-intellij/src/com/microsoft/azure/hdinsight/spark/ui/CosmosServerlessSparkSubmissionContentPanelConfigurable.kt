package com.microsoft.azure.hdinsight.spark.ui

import com.google.common.collect.ImmutableSortedSet
import com.intellij.execution.configurations.RuntimeConfigurationError
import com.intellij.openapi.project.Project
import com.intellij.uiDesigner.core.GridConstraints
import com.microsoft.azure.hdinsight.sdk.cluster.IClusterDetail
import com.microsoft.azure.hdinsight.sdk.common.azure.serverless.AzureSparkServerlessAccount
import com.microsoft.azure.hdinsight.sdk.common.azure.serverless.AzureSparkServerlessClusterManager
import com.microsoft.azure.hdinsight.spark.common.SparkSubmitModel
import com.microsoft.azure.hdinsight.spark.run.configuration.CosmosServerlessSparkSubmitModel
import com.microsoft.azuretools.azurecommons.helpers.NotNull
import com.microsoft.intellij.forms.dsl.panel
import rx.Observable
import java.awt.FlowLayout
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.JPanel
import javax.swing.JTextField

open class CosmosServerlessSparkSubmissionContentPanelConfigurable(project : Project) : SparkSubmissionContentPanelConfigurable(project, false) {

    @NotNull
    override fun getClusterDetails(): ImmutableSortedSet<out IClusterDetail> {
        return ImmutableSortedSet.copyOf({ x, y -> x.title.compareTo(y.title, ignoreCase = true) },
                AzureSparkServerlessClusterManager.getInstance().accounts)
    }

    @NotNull
    override fun getClusterDetailsWithRefresh(): Observable<ImmutableSortedSet<out IClusterDetail>> {
        return Observable.fromCallable { AzureSparkServerlessClusterManager.getInstance().accounts }
                .map { list -> ImmutableSortedSet.copyOf({ x, y -> x.title.compareTo(y.title, ignoreCase = true) }, list) }
    }

    override fun onClusterSelected(@NotNull cluster: IClusterDetail) {
        super.onClusterSelected(cluster)
        this.sparkEventsDirectoryPrefixField.text = (cluster as? AzureSparkServerlessAccount)?.storageRootPath
    }

    override fun getData(@NotNull data: SparkSubmitModel) {
        super.getData(data)

        // Component -> Data
        (data as CosmosServerlessSparkSubmitModel).apply {
            val account = selectedClusterDetail as? AzureSparkServerlessAccount
            if (account != null) {
                tenantId = account.subscription.tenantId
                accountName = account.name
                clusterId = account.id
                livyUri = account.connectionUrl?.toString() ?: ""

                setSparkEventsDirectoryPath(sparkEventsDirectoryField.text)
            }
        }
    }

    override fun setData(@NotNull data: SparkSubmitModel) {
        super.setData(data)
        this.sparkEventsDirectoryField.text = (data as CosmosServerlessSparkSubmitModel).getSparkEventsDirectoryPath()
    }

    override fun getComponent(): JComponent {
        return buildPanel()
    }

    private val sparkEventsPrompt = JLabel("Spark Events directory:").apply {
        toolTipText = "Directory Path for spark events"
    }

    private val sparkEventsDirectoryPrefixField = JLabel("adl://*.azuredatalakestore.net/").apply {
        toolTipText = "DirectoryPath for spark events"
    }

    private val sparkEventsDirectoryField = JTextField("spark-events").apply {
        toolTipText = sparkEventsPrompt.toolTipText
    }

    var sparkEventsDirectory = JPanel(FlowLayout(FlowLayout.LEFT)).apply {
        add(sparkEventsPrompt)
        add(sparkEventsDirectoryPrefixField)
        add(sparkEventsDirectoryField)
    }

    private fun buildPanel() : JPanel {
        this.submissionPanel.clustersSelectionPrompt.text = "ADL account"
        this.submissionPanel.buildPanel()
        val formBuilder = panel {
            columnTemplate {
                col {
                    anchor = GridConstraints.ANCHOR_WEST
                    fill = GridConstraints.FILL_NONE
                }
                col {
                    anchor = GridConstraints.ANCHOR_WEST
                    hSizePolicy = GridConstraints.SIZEPOLICY_WANT_GROW
                    fill = GridConstraints.FILL_HORIZONTAL
                }
            }
            row { c(submissionPanel);}
            row { c(sparkEventsDirectory) }
        }
        return formBuilder.buildPanel()
    }
}