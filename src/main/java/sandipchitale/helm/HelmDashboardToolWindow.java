package sandipchitale.helm;

import com.intellij.ide.plugins.PluginManagerCore;
import com.intellij.openapi.extensions.PluginId;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.SimpleToolWindowPanel;
import com.intellij.ui.jcef.JBCefBrowser;
import com.intellij.ui.jcef.JBCefClient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.util.Objects;

public class HelmDashboardToolWindow {

    public static final String ABOUT_BLANK = "about:blank";
    private static int HELM_DASHBOARD_PORT = 12345;
    private static String HELM_DASHBOARD_URL = "http://127.0.0.1:" + HELM_DASHBOARD_PORT ;

    private static final String X86_64_WINDOWS_HELM_DASHBOARD =
            Objects.requireNonNull(PluginManagerCore.getPlugin(PluginId.getId("sandipchitale.helm-dashboard")))
                    .getPluginPath()
                    .resolve("helm-dashboard")
                    .resolve("x86_64")
                    .resolve("windows")
                    .resolve("helm-dashboard.exe")
                    .toString();

    private static final String X86_64_LINUX_HELM_DASHBOARD =
            Objects.requireNonNull(PluginManagerCore.getPlugin(PluginId.getId("sandipchitale.helm-dashboard")))
                    .getPluginPath()
                    .resolve("helm-dashboard")
                    .resolve("x86_64")
                    .resolve("linux")
                    .resolve("helm-dashboard.exe")
                    .toString();

    private static final String ARM64_DARWIN_HELM_DASHBOARD =
            Objects.requireNonNull(PluginManagerCore.getPlugin(PluginId.getId("sandipchitale.helm-dashboard")))
                    .getPluginPath()
                    .resolve("helm-dashboard")
                    .resolve("arm64")
                    .resolve("darwin")
                    .resolve("helm-dashboard")
                    .toString();

    private final JPanel contentToolWindow;

    private static JBCefBrowser browser;
    private static JButton startHelmDashboardButton;
    private static JButton stopHelmDashboardButton;

    public HelmDashboardToolWindow(Project project)
    {
        this.contentToolWindow = new SimpleToolWindowPanel(true, true);
        this.contentToolWindow.setPreferredSize(new Dimension(1080, 880));

        browser = new JBCefBrowser(ABOUT_BLANK);
        JBCefClient client = browser.getJBCefClient();

        JPanel toolbar = new JPanel();
        toolbar.setLayout(new FlowLayout(FlowLayout.RIGHT, 4, 2));

        startHelmDashboardButton = new JButton("Start Helm Dashboard");
        startHelmDashboardButton.addActionListener((actionEvent) -> {
            HelmDashboardToolWindow.startHelmDashboard();
        });
        toolbar.add(startHelmDashboardButton);

        stopHelmDashboardButton = new JButton("Stop Helm Dashboard");
        stopHelmDashboardButton.addActionListener((actionEvent) -> {
            HelmDashboardToolWindow.stopHelmDashboard();
        });
        toolbar.add(stopHelmDashboardButton);

        contentToolWindow.add(toolbar, BorderLayout.NORTH);
        contentToolWindow.add(browser.getComponent(), BorderLayout.CENTER);

        adjustButtons();
    }

    public JComponent getContent()
    {
        return this.contentToolWindow;
    }

    private static void adjustButtons() {
        startHelmDashboardButton.setVisible(helmDsahboardProcess == null);
        stopHelmDashboardButton.setVisible(helmDsahboardProcess != null);
    }

    private static Process helmDsahboardProcess;

    private static void startHelmDashboard() {
        try {
            helmDsahboardProcess = new ProcessBuilder(
                    X86_64_WINDOWS_HELM_DASHBOARD,
                    "/bind:127.0.0.1",
                    "/port:" + HELM_DASHBOARD_PORT,
                    "/no-browser")
                .redirectErrorStream(true)
                .redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            // Wait for 2 seconds for the helm dashboard to start
            Timer timer = new Timer(2000 , (ActionEvent e) -> {
                browser.loadURL(HELM_DASHBOARD_URL);
            });
            timer.setRepeats(false);
            timer.start();
        } catch (IOException ignore) {
        } finally {
            adjustButtons();
        }
    }

    private static void stopHelmDashboard() {
        try {
            if (helmDsahboardProcess != null) {
                browser.loadURL(ABOUT_BLANK);
                helmDsahboardProcess.destroy();
                helmDsahboardProcess = null;
            }
        } finally {
            adjustButtons();
        }
    }
}