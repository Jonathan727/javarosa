To use this module in an Android Project, you must configure the project directory in `settings.gradle`.

Example `settings.gradle`:

    include ':app', ':javarosa'
    project(':javarosa').setProjectDir(new File('/javarosa/core'))
