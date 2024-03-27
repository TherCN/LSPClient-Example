java \
-Declipse.application=org.eclipse.jdt.ls.core.id1 \
-Dosgi.bundles.defaultStartLevel=4 \
-Declipse.product=org.eclipse.jdt.ls.core.product \
-Dosgi.checkConfiguration=true \
-Dosgi.sharedConfiguration.area=$JDTLS_DIR/config_linux \
-Dosgi.sharedConfiguration.area.readOnly=true \
-Dosgi.configuration.cascaded=true \
-Xms1G \
--add-modules=ALL-SYSTEM \
--add-opens \
java.base/java.util=ALL-UNNAMED \
--add-opens \
java.base/java.lang=ALL-UNNAMED \
-jar \
$JDTLS_DIR/plugins/org.eclipse.equinox.launcher_1.6.700.v20231214-2017.jar \
-data \
/storage/emulated/0/TestProject
