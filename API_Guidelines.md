API Guidelines
===

This document specifies the basic guidelines for extending or adding new functionality to the API.
It should also make working with the API easier and more importantly universal throughout the whole API.

The guidelines in this document must be followed as far as possible in Java and strictly in Kotlin.
Code samples will be provided in Java and Kotlin where necessary.
 
Compatibility with this specification might break with new language developments and/or changes to the guidelines.
In this case the API has to be refactored to meet the new guidelines before a major release.

These guidelines will try to be mostly backwards compatible, but might deprecate parts of the guidelines and remove them in future versions.

Functional Interfaces & `typealiases`
---

To simplify development for both, Java and Kotlin developers, Functional Interfaces (interfaces with just one method) always require the `@FunctionalInterface` annotation and a corresponding `typealias`.
This way, lambdas can be used in both languages.
 
Names for the typealiases are created using the interface and changing it to a performing name. (needs better name!)
  
For example: The interface `AuthenticationPrompt` should be created. 
The Kotlin file for this interface should look like this:

```kotlin
@java.lang.FunctionalInterface
interface AuthenticationPrompt {

    /**
    * Prompts the user for username and password
    * 
    *  @return the credentials of the user, 
       element 1 is the username, element 2 the password 
    */
    fun prompt(): Pair<String, String>

}

typealias AuthenticationPrompter = () -> Pair<String, String>
```

In Java this interface is a lot simpler:

```java
@FunctionalInterface
public interface AuthenticationPrompt {
    
    kotlin.Pair<String, String> prompt();
    
}
```

Kotlin parts of the API using this interface should now define two overloading functions that require this interface.
 
```kotlin

class SomeClass {

    fun addPrompter(authenticationPrompt: AuthenticationPrompt) {
        ...
    }
    
    fun addPrompter(authenticationPrompt: AuthenticationPrompter) {
        this.addPrompter(AuthenticationPrompt.of(authenticationPrompt))
    }

}
``` 

Java parts of the API can ignore the second part of this API. With definitions like this, lambdas can be used for both languages.

```kotlin
val someObject = SomeClass()
someObject.addPrompter({
    Pair("test", "test")
})
```

```java
SomeClass someObject = new SomeClass()
someObject.addPrompter(() -> {
    return new Pair("test", "test")
})
```

Interface Functional Factoring Pattern
---

All interfaces should define a static method `.of(...)` that creates an instance based on functional parameters.
Functions for this method need to be defined as typealiases in Kotlin. 

For the AuthenticationPrompt example this method could look like this (in Kotlin)

```kotlin

interface AuthenticationPrompt {

    typealias Prompter = () -> Pair<String, String>

    fun prompt(): Pair<String, String>
    
    companion object {
    
        @JvmStatic
        fun of(prompter: AuthenticationPrompt.Prompter) 
                : AuthenticationPrompt {
            return object: AuthenticationPrompt {
                override fun prompt() = prompter() 
            }
        }
    }

}
```

This API allows developers to easily implement interface implementations in a functional way, instead of defining a new class every time.

```kotlin
val authenticationPrompt = AuthenticationPrompt.of {
    Pair("test", "test")
}
```