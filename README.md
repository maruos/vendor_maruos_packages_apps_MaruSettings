# MaruSettings

This project is in experiement state to study the difficulty to extract `Maru` modification
to `Settings` to a single app.

## `EXTRA_SETTINGS`

```xml
<intent-filter>
    <action android:name="com.android.settings.action.EXTRA_SETTINGS" />
</intent-filter>

<meta-data
    android:name="com.android.settings.FRAGMENT_CLASS"
    android:value="com.maru.settings.desktop.DesktopDashboardFragment" />

<meta-data
    android:name="com.android.settings.category"
    android:value="com.android.settings.category.ia.homepage" />

<meta-data
    android:name="com.android.settings.title"
    android:value="@string/desktop_dashboard_title" />

<meta-data
    android:name="com.android.settings.summary"
    android:value="@string/desktop_dashboard_summary" />
```

If we add `intent-filter` with action `com.android.settings.action.EXTRA_SETTINGS` for `Activity`,
the `Settings` will add this `Activity` to its categories. And if we add `meta-data` with key
`com.android.settings.category` and value `com.android.settings.category.ia.homepage`, the
`Settings` will add `Activity` to homepage dashboard, by searching this category from added
`categories` list.

And we add the `meta-data` with key `com.android.settings.summary` with specific value, it will
shown to the homepage dashboard.

## `SettingsLib`

We use `SettingsLib` as library to create setting view. This library needs some runtime permissions
to query user information. So we add the shared uid `android.uid.system` to skip the system checking
for critic operations. Maybe we should assign the permission to the app from config file to replace
shared uid.

## View

We copy `SwitchBar` and `ToggleButton` from the `Settings`, and use it to our app.

## How to use

Create directory `apps` in `vendor/maruos`, and add a `Android.mk` with following content:

```Makefile
LOCAL_PATH := $(call my-dir)

include $(call all-makefiles-under, $(LOCAL_PATH))
```

Then use below command to clone project to `vendor/maruos/apps`:

```shell
git clone https://github.com/utzcoz/vendor_maruos_packages_apps_MaruSettings.git MaruSettings
```

The next thing we should do is to add `MaruSettings` to `vendor/maruos/device-maru.mk` 
`PRODUCT_PACKAGES`:

```Makefile
PRODUCT_PACKAGES += \
    MaruSettings
```

And last, we just need to build the system, and flash the images to the device. We can see 
another `Desktop` entry on `Settings` dashboard homepage.