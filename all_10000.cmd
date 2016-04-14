start sbt -Daspect.rest.port=8888 -Dakka.remote.netty.tcp.port=10000 -Dakka.cluster.seed-nodes.0=akka.tcp://aspect@127.0.0.1:10000 -Dakka.cluster.roles.0=all run




