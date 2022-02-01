Dubbo plugin for JetBrains IDEs
==============================

<!-- Plugin description -->
**Dubbo plugin** is a plugin for JetBrains IDE to execute Dubbo requests in HTTP file.

The following features are available for Dubbo:

* Dubbo Request
* Live templates: dubbo
* Dubbo Endpoint support: Java and Kotlin

<!-- Plugin description end -->

# Dubbo request demo

```http request
### Dubbo request
//@name sayHi
Dubbo 127.0.0.1:20880/GreetingsService?method=sayHi(java.lang.String)
Content-Type: application/json

"Jackie"
```

# References

* Apache Dubbo: https://dubbo.apache.org/
* JetBrains HTTP client: https://www.jetbrains.com/help/idea/http-client-in-product-code-editor.html


              

