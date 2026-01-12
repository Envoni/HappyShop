package ci553.happyshop.ui;

import ci553.happyshop.utility.UIStyle;
import javafx.scene.Scene;

import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class AppTheme {

    private static final class Entry {
        final WeakReference<Scene> sceneRef;
        final Runnable onThemeChanged;

        Entry(Scene scene, Runnable onThemeChanged) {
            this.sceneRef = new WeakReference<>(scene);
            this.onThemeChanged = onThemeChanged;
        }
    }

    private static final List<Entry> entries = new ArrayList<>();

    private AppTheme() {
        throw new UnsupportedOperationException("AppTheme is a utility class");
    }

    // Backwards-compatible register
    public static void register(Scene scene) {
        register(scene, null);
    }

    // register a Scene and an optional hook to re-apply inline theme styles
    public static void register(Scene scene, Runnable onThemeChanged) {
        if (scene == null) return;
        for (Entry e : entries) {
            Scene s = e.sceneRef.get();
            if (s == scene) {
                apply(scene);
                if (onThemeChanged != null) onThemeChanged.run();
                return;
            }
        }
        entries.add(new Entry(scene, onThemeChanged));
        apply(scene);
        if (onThemeChanged != null) onThemeChanged.run();
    }

    // Apply the current UIStyle theme CSS to a specific Scene
    public static void apply(Scene scene) {
        if (scene == null) return;

        String cssPath = switch (UIStyle.getTheme()) {
            case DARK -> "/css/app-dark.css";
            case COLOURFUL -> "/css/app-colourful.css";
            case LIGHT -> "/css/app-light.css";
        };

        URL url = AppTheme.class.getResource(cssPath);
        if (url == null) {
            throw new IllegalStateException("Missing CSS resource on classpath: " + cssPath);
        }

        String css = url.toExternalForm();
        System.out.println("Applying theme: " + UIStyle.getTheme());
        System.out.println("Before setAll: " + scene.getStylesheets());
        System.out.println("Using CSS: " + css);

        scene.getStylesheets().setAll(css);
        System.out.println("After setAll: " + scene.getStylesheets());

        // force CSS to be recalculated now
        if (scene.getRoot() != null) {
            scene.getRoot().applyCss();
            scene.getRoot().requestLayout();
        }
    }

    // apply theme to all registered scenes + run their hooks
    public static void refreshAll() {
        Iterator<Entry> it = entries.iterator();
        while (it.hasNext()) {
            Entry e = it.next();
            Scene scene = e.sceneRef.get();
            if (scene == null) {
                it.remove();
                continue;
            }
            apply(scene);
            if (e.onThemeChanged != null) {
                e.onThemeChanged.run();
            }
        }
    }
}
