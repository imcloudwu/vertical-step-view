# Vertical-Step-View
An Android custom view component

<img src="https://github.com/imcloudwu/vertical-step-view/blob/master/screen_shot.gif" width="200">

# Setup
##### Step 1. Add the JitPack repository to your build file
###### Add it in your root build.gradle at the end of repositories:
```
allprojects {
    repositories {
        ...
        maven { url 'https://jitpack.io' }
    }
}
```
##### Step 2. Add the dependency
```
dependencies {
    implementation 'com.github.imcloudwu:vertical-step-view:1.2.0'
}
```

# Usage
#### Layout
```
    <!--Make it scrollable-->
    <ScrollView
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:fillViewport="true">

        <!--Put it in th center-->
        <LinearLayout
            android:layout_width="match_parent"
            android:layout_height="wrap_content"
            android:gravity="center">

            <!--Support textColor and textSize change-->
            <com.imcloudwu.android.component.VerticalStepView
                android:id="@+id/verticalStepView"
                android:layout_width="wrap_content"
                android:layout_height="wrap_content"
                app:textColor="@color/teal_200"
                app:textSize="24sp" />

        </LinearLayout>

    </ScrollView>
```
#### Set and move
```
val data:List<String> = ...
verticalStepView.setSteps(data)
verticalStepView.move(100)
```
