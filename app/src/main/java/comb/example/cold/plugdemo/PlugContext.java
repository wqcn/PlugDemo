package comb.example.cold.plugdemo;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import java.io.File;
import java.lang.reflect.Method;

import dalvik.system.DexClassLoader;

/**
 * 加载SD卡上的apk安装包
 * 2016-03-14
 */
public class PlugContext extends ContextWrapper {

    private Context context;
    private AssetManager mAssetManager = null;
    private Resources mResources;
    private String mDexPath;
    private DexClassLoader localDexClassLoader;
    private Resources.Theme mTheme;
    private String packageName;
    private LayoutInflater mLayoutInflater;


    /*********
     * 获取apk里的资源
     ***********/
    public static final String LAYOUT = "layout";
    public static final String ID = "id";
    public static final String DRAWABLE = "drawable";
    public static final String STYLE = "style";
    public static final String STRING = "string";
    public static final String COLOR = "color";
    public static final String DIMEN = "dimen";


    public PlugContext(Context base) {
        super(base);
        this.context = base;
    }

    /**
     * 加载apk文件
     *
     * @param apkPath        apk文件的路径
     * @param apkPackageName apk插件的包名
     */
    public void loadApk(String apkPath, String apkPackageName) {
        mDexPath = apkPath;
        packageName = apkPackageName;
        loadResources();
        ClassLoader localClassLoader = ClassLoader.getSystemClassLoader();
        //加载的文件的要拷贝的文件夹, Context的getDir(String name, int mode)方法返回在data/data/下的dex文件,没有则创建
        File dexOutputDir = context.getDir("dex", 0);
        localDexClassLoader = new DexClassLoader(apkPath, dexOutputDir.getAbsolutePath(), null, localClassLoader);
    }

    /**
     * 获取插件里的类
     *
     * @param className 包名+类名
     * @return Class
     * @throws ClassNotFoundException
     */
    public Class getClassInPlug(String className) throws ClassNotFoundException {
        Class plugClass = localDexClassLoader.loadClass(className);
        return plugClass;
    }

    /**
     * 获取插件里的方法
     *
     * @param className  包名+类名
     * @param methodName 方法名
     * @param params     参数数组
     * @return
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public Method getMethod(String className, String methodName, Class[] params) throws NoSuchMethodException, ClassNotFoundException, IllegalAccessException, InstantiationException {
        Class plugClass = localDexClassLoader.loadClass(className);
        // 下面是反射到方法
        Object instance = plugClass.newInstance();
        Method method = plugClass.getMethod(methodName, params);
        return method;
    }

    /**
     * 获取插件里的方法
     *
     * @param plugClass  类
     * @param methodName 方法名
     * @param params     参数数组
     * @return
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws NoSuchMethodException
     */
    public Method getMethod(Class plugClass, String methodName, Class[] params) throws IllegalAccessException, InstantiationException, NoSuchMethodException {
        // 下面是反射到方法
        Object instance = plugClass.newInstance();
        Method method = plugClass.getMethod(methodName, params);
        return method;
    }

    /**
     * 获取对应的AssetManager,Resources
     */
    protected void loadResources() {
        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Method addAssetPath = assetManager.getClass().getMethod("addAssetPath", String.class);
            addAssetPath.invoke(assetManager, mDexPath);
            mAssetManager = assetManager;
        } catch (Exception e) {
            e.printStackTrace();
        }
        Resources superRes = super.getResources();
        mResources = new Resources(mAssetManager, superRes.getDisplayMetrics(),
                superRes.getConfiguration());
        mTheme = mResources.newTheme();
        mTheme.setTo(super.getTheme());
        getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    /**
     * 获取资源的id
     *
     * @param name 资源名
     * @param type 资源类型(drawable,layout,color...)
     * @return
     */
    public int getResId(String name, String type) {
        return mResources.getIdentifier(name, type, packageName);
    }

    public int getId(String name) {
        return mResources.getIdentifier(name, ID, packageName);
    }

    public View getLayout(String name) {
        return mLayoutInflater.inflate(getResId(name, LAYOUT), null);
    }

    public String getString(String name) {
        return mResources.getString(getResId(name, STRING));
    }


    public Drawable getDrawable(String name) {
        return mResources.getDrawable(getResId(name, DRAWABLE));
    }

    public int getColor(String name) {
        return mResources.getColor(getResId(name, COLOR));
    }

    public int getStyle(String name) {
        return getResId(name, STYLE);
    }

    public float getDimen(String name) {
        return mResources.getDimension(getResId(name, DIMEN));
    }

    @Override
    public Object getSystemService(String name) {
        if (LAYOUT_INFLATER_SERVICE.equals(name)) {
            if (mLayoutInflater == null) {
                try {
                    Class<?> cls = Class
                            .forName("com.android.internal.policy.PolicyManager");
                    Method m = cls.getMethod("makeNewLayoutInflater",
                            Context.class);
                    mLayoutInflater = (LayoutInflater) m.invoke(null, this);
                } catch (Throwable e) {
                    e.printStackTrace();
                }
            } else {
                return mLayoutInflater;
            }
        }
        return super.getSystemService(name);
    }

    /**
     * 重写getResources方法,屏蔽原来的那一个
     */
    @Override
    public Resources getResources() {
        if (mResources != null) {
            return mResources;
        }
        return super.getResources();
    }

    @Override
    public AssetManager getAssets() {
        if (mAssetManager != null) {
            return mAssetManager;
        }
        return super.getAssets();
    }
}
