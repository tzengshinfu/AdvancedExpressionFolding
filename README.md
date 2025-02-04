# Advanced Java Folding Plus

Modern JVM languages such as Kotlin, Groovy, Scala and some others offer many language features that let you
write code in a more concise and expressive manner. These features include type inference, properties,
interpolated string, range and tuple literals, enhanced operators, clojures, implicits, smart casts and any more.

This plugin extends the IDE’s folding features to emulate some of these modern languages’ features helping
fight verbosity.

For more information, read the [blog post](https://medium.com/@andrey_cheptsov/making-java-code-easier-to-read-without-changing-it-adeebd5c36de).

### Custom Color Scheme

For more clarity, you may try to adjust your color scheme: go to **Settings** | **Editor** |
**Colors &amp; Fonts** | **General**, select **Folded text**, uncheck the **Background** color, and change the
**Foreground** color to #000091 for the default scheme and #7CA0BB for Darcula.

### Disabling Certain Foldings

To disable certain types of folding, go to **Settings** | **Editor** |
**General** | **Code Folding** | **Fold by default:** | **Advanced Java Folding Plus**.

## About this plugin

This plugin is fork of [Advanced Java Folding](https://github.com/cheptsov/AdvancedExpressionFolding).  
The original plugin focuses on simplifying verbose syntax and reducing the mental burden on developers.  
On this basis, [Advanced Java Folding Plus](https://github.com/tzengshinfu/AdvancedExpressionFolding) adds quick input functions to reduce the burden on developers' fingers.

### New features

- #### Input operator (+ - * / ===) to invoke the corresponding method (experiment)

    You can now directly enter an operator to call the corresponding method (in the form of a code completion autopopup).  
    For example, consider the following code:  
    ```Java
    BigDecimal a = new BigDecimal("1.0");
    BigDecimal b = new BigDecimal("2.0");
    BigDecimal c = a
    ```
    When entering `+` after `a` on the third line, an autopopup for the corresponding method `add(BigDecimal augend)` will automatically appear.  
    Upon selection, `a +` will be converted to `a.add()`, with the cursor positioned inside the parentheses awaiting the input of parameter `b`.  
    Alternatively, if `a` and `b` are already written, entering `+` between them will also trigger the autopopup for `add(BigDecimal augend)`.  
    Upon selection, `a + b` will be converted to `a.add(b)`.  

    Currently supported operations:  
    - **BigDecimal/BigInteger**: `+` → `add`, `-` → `subtract`, `*` → `multiply`, `/` → `divide`  
    - **All classes**: `===` → `equals`

- #### Collapse immediately after invoking the corresponding method (experiment)

    To reduce visual clutter, the method call will automatically collapse into operator form after invocation.  
    For example, when `a.add(b)` is completed and the cursor moves out of the parentheses, it will automatically collapse into `a + b`.
