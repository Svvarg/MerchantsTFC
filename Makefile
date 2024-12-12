FORGE_VER := 1.7.10-10.13.4.1614-1.7.10
GRADLE_CACHE := ${HOME}/.gradle/caches/

srg-files:
	cp $(GRADLE_CACHE)/minecraft/net/minecraftforge/forge/$(FORGE_VER)/unpacked/conf/packaged.srg ./run/
	cp $(GRADLE_CACHE)/minecraft/net/minecraftforge/forge/$(FORGE_VER)/unpacked/conf/methods.csv ./run/
	cp $(GRADLE_CACHE)/minecraft/net/minecraftforge/forge/$(FORGE_VER)/unpacked/conf/fields.csv ./run/


run-client: srg-files
	gralde runClient
