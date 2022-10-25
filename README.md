# AlgocodePlugin2022 (2023?)

## запуск для отладки в Inteliji

1. Скачайте [spigot](https://getbukkit.org/get/7d4d4901bb1f641da6a9882c69d5fd54)
2. Создайте внутри проекта папку `/server`, сложите туда скачанный `jar` файл
3. Попробуйте запустить этот файл (правый клик)
   1. не забудьте сделать ему рабочей директорией папку `/server`
   2. согласитесь с eula в созданном файле 
4. Запустите `./gradlew build` для плагина
5. Отредактируйте конфигурацию для запуска сервера:
   1. <img width="1152" alt="config_all" src="https://user-images.githubusercontent.com/51089819/197866227-2fc1f571-d69f-4ba8-a771-9dba559c3ef1.png"> обратите внимание на before launch
   2. <img width="463" alt="config_gradle" src="https://user-images.githubusercontent.com/51089819/197866253-a4c4149b-c3eb-4171-99fb-5ef6efa52d17.png">
   3. <img width="695" alt="config_smoke" src="https://user-images.githubusercontent.com/51089819/197866271-aa20b265-f3cd-48d4-a917-ece73e985c57.png">
   4. Эта конфигурация выгружает дебажебельную версию плагина к плагинам запускаемого сервера
6. PROFIT! 🎉

...
IN PROGRESS
...
