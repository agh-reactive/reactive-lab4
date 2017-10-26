
name := """reactive-lab4"""

version := "1.2"

scalaVersion := "2.12.3"

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.5.4",
  "com.typesafe.akka" %% "akka-persistence" % "2.5.4",
  "org.iq80.leveldb"            % "leveldb"          % "0.9",
  "org.fusesource.leveldbjni"   % "leveldbjni-all"   % "1.8" 
)
  

EclipseKeys.createSrc := EclipseCreateSrc.Default + EclipseCreateSrc.Resource

