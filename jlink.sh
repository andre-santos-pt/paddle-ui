rm -rf standalone

/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home/bin/jlink --module-path "/Library/Java/JavaVirtualMachines/jdk-11.0.4.jdk/Contents/Home/jmods:lib:../paddle/lib"  --add-modules pt.iscte.paddle.ide --output binaries/standalone

echo 'bin/java  -XstartOnFirstThread -Xdock:name="Paddle" --module pt.iscte.paddle.ide  -XstartOnFirstThread -Xdock:name="Paddle"' > standalone/launch.sh
chmod 707 standalone/launch.sh

