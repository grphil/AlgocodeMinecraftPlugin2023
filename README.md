# AlgocodePlugin2022 (2023?)

## запуск для отладки в Inteliji

1. Скачайте [spigot](https://getbukkit.org/get/7d4d4901bb1f641da6a9882c69d5fd54)
2. Создайте внутри проекта папку `/server`, сложите туда скачанный `jar` файл
3. Попробуйте запустить этот файл (правый клик)
   1. не забудьте сделать ему рабочей директорией папку `/server`
   2. согласитесь с eula в созданном файле 
4. Запустите `./gradlew build` для плагина
5. Отредактируйте конфигурацию для запуска сервера:
   1. ![](readme_sources/config_all.png) обратите внимание на before launch
   2. ![](readme_sources/config_gradle.png) 
   3. ![](readme_sources/config_smoke.png)
   4. Эта конфигурация выгружает дебажебельную версию плагина к плагинам запускаемого сервера
6. PROFIT! 🎉

...
IN PROGRESS
...