"use strict";(self.webpackChunkdoc=self.webpackChunkdoc||[]).push([[242],{3905:(e,a,t)=>{t.d(a,{Zo:()=>s,kt:()=>m});var n=t(7294);function r(e,a,t){return a in e?Object.defineProperty(e,a,{value:t,enumerable:!0,configurable:!0,writable:!0}):e[a]=t,e}function i(e,a){var t=Object.keys(e);if(Object.getOwnPropertySymbols){var n=Object.getOwnPropertySymbols(e);a&&(n=n.filter((function(a){return Object.getOwnPropertyDescriptor(e,a).enumerable}))),t.push.apply(t,n)}return t}function o(e){for(var a=1;a<arguments.length;a++){var t=null!=arguments[a]?arguments[a]:{};a%2?i(Object(t),!0).forEach((function(a){r(e,a,t[a])})):Object.getOwnPropertyDescriptors?Object.defineProperties(e,Object.getOwnPropertyDescriptors(t)):i(Object(t)).forEach((function(a){Object.defineProperty(e,a,Object.getOwnPropertyDescriptor(t,a))}))}return e}function l(e,a){if(null==e)return{};var t,n,r=function(e,a){if(null==e)return{};var t,n,r={},i=Object.keys(e);for(n=0;n<i.length;n++)t=i[n],a.indexOf(t)>=0||(r[t]=e[t]);return r}(e,a);if(Object.getOwnPropertySymbols){var i=Object.getOwnPropertySymbols(e);for(n=0;n<i.length;n++)t=i[n],a.indexOf(t)>=0||Object.prototype.propertyIsEnumerable.call(e,t)&&(r[t]=e[t])}return r}var c=n.createContext({}),p=function(e){var a=n.useContext(c),t=a;return e&&(t="function"==typeof e?e(a):o(o({},a),e)),t},s=function(e){var a=p(e.components);return n.createElement(c.Provider,{value:a},e.children)},u={inlineCode:"code",wrapper:function(e){var a=e.children;return n.createElement(n.Fragment,{},a)}},d=n.forwardRef((function(e,a){var t=e.components,r=e.mdxType,i=e.originalType,c=e.parentName,s=l(e,["components","mdxType","originalType","parentName"]),d=p(t),m=r,k=d["".concat(c,".").concat(m)]||d[m]||u[m]||i;return t?n.createElement(k,o(o({ref:a},s),{},{components:t})):n.createElement(k,o({ref:a},s))}));function m(e,a){var t=arguments,r=a&&a.mdxType;if("string"==typeof e||r){var i=t.length,o=new Array(i);o[0]=d;var l={};for(var c in a)hasOwnProperty.call(a,c)&&(l[c]=a[c]);l.originalType=e,l.mdxType="string"==typeof e?e:r,o[1]=l;for(var p=2;p<i;p++)o[p]=t[p];return n.createElement.apply(null,o)}return n.createElement.apply(null,t)}d.displayName="MDXCreateElement"},5363:(e,a,t)=>{t.r(a),t.d(a,{assets:()=>c,contentTitle:()=>o,default:()=>u,frontMatter:()=>i,metadata:()=>l,toc:()=>p});var n=t(7462),r=(t(7294),t(3905));const i={},o="Getting started",l={unversionedId:"getting_started",id:"getting_started",title:"Getting started",description:"You will create and run a K5 runtime in a minute.",source:"@site/docs/02-getting_started.md",sourceDirName:".",slug:"/getting_started",permalink:"/karaf5/docs/getting_started",draft:!1,tags:[],version:"current",sidebarPosition:2,frontMatter:{},sidebar:"tutorialSidebar",previous:{title:"Welcome to K5",permalink:"/karaf5/docs/intro"},next:{title:"Architecture",permalink:"/karaf5/docs/architecture"}},c={},p=[{value:"Installing karaf-build CLI",id:"installing-karaf-build-cli",level:2},{value:"karaf-build.json",id:"karaf-buildjson",level:2},{value:"Runtime package",id:"runtime-package",level:2},{value:"Runtime jar",id:"runtime-jar",level:2},{value:"Runtime archive",id:"runtime-archive",level:2}],s={toc:p};function u(e){let{components:a,...t}=e;return(0,r.kt)("wrapper",(0,n.Z)({},s,t,{components:a,mdxType:"MDXLayout"}),(0,r.kt)("h1",{id:"getting-started"},"Getting started"),(0,r.kt)("p",null,"You will create and run a K5 runtime in a minute."),(0,r.kt)("p",null,"In this first run, we will create an empty (without any module) K5 runtime (using the command line) and run it."),(0,r.kt)("p",null,"It's the thinest runtime you can create, just starting the K5 boot part with K5 core services."),(0,r.kt)("p",null,"You have several options to create a K5 runtime:"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"CLI (command line)"),(0,r.kt)("li",{parentName:"ul"},"Maven plugin"),(0,r.kt)("li",{parentName:"ul"},"Gradle plugin"),(0,r.kt)("li",{parentName:"ul"},"REST service"),(0,r.kt)("li",{parentName:"ul"},"K5 Creator")),(0,r.kt)("p",null,"For this first example, we are going to use the command line."),(0,r.kt)("h2",{id:"installing-karaf-build-cli"},"Installing karaf-build CLI"),(0,r.kt)("p",null,"You can find the command line binary in ",(0,r.kt)("inlineCode",{parentName:"p"},"tool/cli/target")," folder once you built it.\nYou can also directly download CLI binaries here:"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://repository.apache.org/content/groups/snapshots/org/apache/karaf/tooling/karaf-build-5.0-SNAPSHOT.tar.gz"},"karaf-build-5.0-SNAPSHOT.tar.gz")),(0,r.kt)("li",{parentName:"ul"},(0,r.kt)("a",{parentName:"li",href:"https://repository.apache.org/content/groups/snapshots/org/apache/karaf/tooling/karaf-build-5.0-SNAPSHOT.tar.gz"},"karaf-build-5.0-SNAPSHOT.zip"))),(0,r.kt)("p",null,"The installation of karaf-build CLI is a simple process of extracting the archive and adding ",(0,r.kt)("inlineCode",{parentName:"p"},"bin")," folder with the ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build")," command to the ",(0,r.kt)("inlineCode",{parentName:"p"},"PATH"),"."),(0,r.kt)("p",null,"Detailed steps are:"),(0,r.kt)("ul",null,(0,r.kt)("li",{parentName:"ul"},"Have a JDK installation on your system. Either set the ",(0,r.kt)("inlineCode",{parentName:"li"},"JAVA_HOME")," environment variable pointing to your JDK installation or have ",(0,r.kt)("inlineCode",{parentName:"li"},"java")," executable on your ",(0,r.kt)("inlineCode",{parentName:"li"},"PATH"),"."),(0,r.kt)("li",{parentName:"ul"},"Extract karaf-cli distribution archive in any directory",(0,r.kt)("pre",{parentName:"li"},(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ unzip karaf-build-5.0-SNAPSHOT.zip\n")),(0,r.kt)("pre",{parentName:"li"},(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ tar zxvf karaf-build-5.0-SNAPSHOT.tar.gz\n")),"Alternatively use your preferred archive extraction tool."),(0,r.kt)("li",{parentName:"ul"},"Add the ",(0,r.kt)("inlineCode",{parentName:"li"},"bin")," directory of the created directory ",(0,r.kt)("inlineCode",{parentName:"li"},"karaf-build-5.0-SNAPSHOT")," to the ",(0,r.kt)("inlineCode",{parentName:"li"},"PATH")," environment variable."),(0,r.kt)("li",{parentName:"ul"},"Confirm with ",(0,r.kt)("inlineCode",{parentName:"li"},"karaf-build --help")," in a new shell. The result should look similar to:",(0,r.kt)("pre",{parentName:"li"},(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"usage: karaf-build [package|jar|archive]\nDefault build action is package\n-f,--file <arg>   Location of the karaf-build.json file\n-h,--help         print this message\n")))),(0,r.kt)("h2",{id:"karaf-buildjson"},"karaf-build.json"),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build")," command use a ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build.json")," file describing your runtime."),(0,r.kt)("p",null,"In the directory of your choice, create the following ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build.json")," file:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-json"},'{\n  "name": "my-runtime",\n  "dependencies": [\n    "k5:karaf-boot",\n    "k5:karaf-banner"\n  ]\n}\n')),(0,r.kt)("p",null,"We will create ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime")," with just the ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-boot")," module (minimal piece for a K5 runtime) and ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-banner")," service module (optional) that just display a fancy message at runtime startup."),(0,r.kt)("p",null,"We are now ready to create our runtime distribution. We have basically three options:"),(0,r.kt)("ol",null,(0,r.kt)("li",{parentName:"ol"},'Create a "exploded" runtime folder.'),(0,r.kt)("li",{parentName:"ol"},'Create a "uber jar" ready to be executed.'),(0,r.kt)("li",{parentName:"ol"},"Create a archive (zip)")),(0,r.kt)("h2",{id:"runtime-package"},"Runtime package"),(0,r.kt)("p",null,"Let's create the ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime")," package using ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build package"),":"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ karaf-build package\noct. 01, 2022 5:43:42 PM org.apache.karaf.tooling.common.Runtime <init>\nINFOS: Creating Karaf runtime package in folder my-runtime\n")),(0,r.kt)("p",null,"A ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime")," directory has been created. To launch ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime"),", you can go in the ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime")," directory and do ",(0,r.kt)("inlineCode",{parentName:"p"},"java -jar karaf-boot-5.0-SNAPSHOT.jar"),":"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ cd my-runtime\n$ java -jar karaf-boot-5.0-SNAPSHOT.jar\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.Main main\nINFOS: Starting runtime in exploded mode\nKaraf lib: /Users/jbonofre/test/my-runtime\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding config service (-2147483647)\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding lifecycle service (-1000)\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding classloader-service service (-1000)\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding karaf-banner service (2147483647)\noct. 01, 2022 5:45:09 PM org.apache.karaf.banner.WelcomeBannerService onRegister\nINFOS:  \n        __ __                  ____\n       / //_/____ __________ _/ __/\n      / ,<  / __ `/ ___/ __ `/ /_\n     / /| |/ /_/ / /  / /_/ / __/\n    /_/ |_|\\__,_/_/   \\__,_/_/\n\n  Apache Karaf 5.x\n\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2\nINFOS: Starting services\noct. 01, 2022 5:45:09 PM org.apache.karaf.boot.service.KarafLifeCycleService start\nINFOS: Starting lifecycle service\n")),(0,r.kt)("h2",{id:"runtime-jar"},"Runtime jar"),(0,r.kt)("p",null,"You can also create a runtime executable uber jar using ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build jar"),":"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ karaf-build jar\n")),(0,r.kt)("p",null,"You now have ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime/my-runtime.jar")," executable jar file. This jar contains everything you need and you can directly run it:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ java -jar my-runtime.jar\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.Main main\nINFOS: Starting runtime in exploded mode\nKaraf lib: /Users/jbonofre/test\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding config service (-2147483647)\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding lifecycle service (-1000)\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding classloader-service service (-1000)\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding karaf-banner service (2147483647)\noct. 01, 2022 5:56:04 PM org.apache.karaf.banner.WelcomeBannerService onRegister\nINFOS:  \n        __ __                  ____\n       / //_/____ __________ _/ __/\n      / ,<  / __ `/ ___/ __ `/ /_\n     / /| |/ /_/ / /  / /_/ / __/\n    /_/ |_|\\__,_/_/   \\__,_/_/\n\n  Apache Karaf 5.x\n\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2\nINFOS: Starting services\noct. 01, 2022 5:56:04 PM org.apache.karaf.boot.service.KarafLifeCycleService start\nINFOS: Starting lifecycle service\n")),(0,r.kt)("h2",{id:"runtime-archive"},"Runtime archive"),(0,r.kt)("p",null,"Finally, you can create a zip archive with ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf-build archive"),":"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ karaf-build archive\noct. 01, 2022 6:00:27 PM org.apache.karaf.tooling.common.Runtime <init>\nINFOS: Creating Karaf runtime package in folder my-runtime\noct. 01, 2022 6:00:27 PM org.apache.karaf.tooling.common.Runtime createArchive\nINFOS: Creating Karaf runtime archive\n")),(0,r.kt)("p",null,"You now have ",(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime/my-runtime.zip")," file, that you can extract in the directory of your choice:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ mv my-runtime/my-runtime.zip temp\n$ cd temp\n$ unzip my-runtime.zip\nArchive:  my-runtime.zip\n   creating: bin/\n  inflating: bin/karaf.sh\n  inflating: karaf-banner-5.0-SNAPSHOT.jar\n  inflating: karaf-boot-5.0-SNAPSHOT.jar\n")),(0,r.kt)("p",null,(0,r.kt)("inlineCode",{parentName:"p"},"my-runtime")," archive contains everything you need, including simple ",(0,r.kt)("inlineCode",{parentName:"p"},"karaf.sh")," script to start the runtime:"),(0,r.kt)("pre",null,(0,r.kt)("code",{parentName:"pre",className:"language-bash"},"$ ./bin/karaf.sh\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.Main main\nINFOS: Starting runtime in exploded mode\nKaraf lib: /Users/jbonofre/test/temp\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding config service (-2147483647)\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding lifecycle service (-1000)\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding classloader-service service (-1000)\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry add\nINFOS: Adding karaf-banner service (2147483647)\noct. 01, 2022 6:04:23 PM org.apache.karaf.banner.WelcomeBannerService onRegister\nINFOS:  \n        __ __                  ____\n       / //_/____ __________ _/ __/\n      / ,<  / __ `/ ___/ __ `/ /_\n     / /| |/ /_/ / /  / /_/ / __/\n    /_/ |_|\\__,_/_/   \\__,_/_/\n\n  Apache Karaf 5.x\n\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.ServiceRegistry lambda$start$2\nINFOS: Starting services\noct. 01, 2022 6:04:23 PM org.apache.karaf.boot.service.KarafLifeCycleService start\nINFOS: Starting lifecycle service\n")),(0,r.kt)("p",null,"You can see how Karaf is easy and fast to start with."),(0,r.kt)("p",null,"You are now ready to create your runtimes for your existing applications, or eventually create a new application using the K5 services."),(0,r.kt)("p",null,"You can take a look on the ",(0,r.kt)("a",{parentName:"p",href:"/examples"},"examples")," and available ",(0,r.kt)("a",{parentName:"p",href:"services"},"K5 services"),"."))}u.isMDXComponent=!0}}]);