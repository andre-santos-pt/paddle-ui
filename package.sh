/Library/Java/JavaVirtualMachines/jdk-14.0.1.jdk/Contents/Home/bin/jpackage -d binaries --module-path lib:../paddle/lib --name PaddleIDE --module pt.iscte.paddle.ide --type dmg  --verbose --java-options '--enable-preview -XstartOnFirstThread'


# java --module-path "lib:../paddle/lib/" -XstartOnFirstThread -Xdock:name="Paddle" -m pt.iscte.paddle.ide/pt.iscte.paddle.ide.PaddleIDE

