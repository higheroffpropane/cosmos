# Retrofit, OkHttp, Coil, Room, Hilt and Coroutines ship their own consumer
# rules; only project-specific keeps live here.

# Keep line numbers for readable release stack traces.
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# kotlinx.serialization: keep generated serializers for @Serializable classes
# (DTOs in core:network and type-safe navigation routes in feature modules).
-keepattributes RuntimeVisibleAnnotations,AnnotationDefault

-keepclassmembers @kotlinx.serialization.Serializable class ** {
    *** Companion;
}
-if @kotlinx.serialization.Serializable class **
-keepclassmembers class <1>$Companion {
    kotlinx.serialization.KSerializer serializer(...);
}
-keepclasseswithmembers class **$$serializer {
    *** INSTANCE;
}

# Retrofit interface methods are looked up reflectively.
-keepattributes Signature, InnerClasses, EnclosingMethod
