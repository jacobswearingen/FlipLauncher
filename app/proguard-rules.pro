# MainActivity is the launcher entry point
-keep class com.jacobswearingen.fliplauncher.MainActivity { *; }

# NotificationService is bound by the system via the manifest
-keep class com.jacobswearingen.fliplauncher.NotificationService { *; }

# Fragments are instantiated by name from the navigation graph XML
-keep class com.jacobswearingen.fliplauncher.**Fragment { *; }

# ViewModels are instantiated by reflection via ViewModelProvider
-keep class com.jacobswearingen.fliplauncher.**ViewModel { *; }

# Suppress notes about missing classes in unused library code
-dontnote **
