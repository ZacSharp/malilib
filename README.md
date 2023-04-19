Forge port note
==============
**Do not report problems to Masa. This is not an offical version of MaLiLib.**<br>
**Report any problems [here](https://github.com/ZacSharp/malilib-forge/issues).**

This is a fork of MaLiLib created by me (@ZacSharp) so I could use Litematica
on Forge 1.16.5 and later 1.17.1. While I did decide to publish my work and am
likely to provide more ported versions than I personally need along with some
level of support, personal usage is still the main motivation so please don't expect
the same level of support as for the official versions.

If you want an official version you will have to either use Fabric or be patient.
Masa has plans to support both loaders with the rewritten codebase, but so far
(April 2023) there is no timeline.

MaLiLib
==============
malilib is a library mod used by masa's LiteLoader mods. It contains some common code previously
duplicated in most of the mods, such as multi-key capable keybinds, configuration GUIs etc.

Downloads / Compiled builds
=========
Have a look at the [CI builds](https://github.com/ZacSharp/malilib-forge/actions).
Click on the topmost entry for your version of Minecraft and
scroll down to "Artifacts". If you are logged in to GitHub you can click on
"Artifacts.zip" to download a zip containing the built jar.

Compiling
=========
* Clone the repository
* Open a command prompt/terminal to the repository directory
* run 'gradlew build'
* The built jar file will be in build/libs/