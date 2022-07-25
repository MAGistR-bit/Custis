import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;

public class Program {

    // Задаем максимальный размер файлов в байтах
    static final int maxLengthSplitFile = 1024 * 1024 * 50;
    // Задаем максимальный размер кучи в байтах
    static final int maxHeapMemory = maxLengthSplitFile * 5;

    // Максимальное количество разделенных файлов (split file)
    static final int maxCountSplitFile = 20;

    public static void main(String[] args) throws Exception{
        // Генерируем файл и возвращаем путь до директории с файлом
        var pathDB = GeneratorBigFile();
        fileSorting(pathDB);
    }

    private static void fileSorting(String pathDB) {
        // Если начальный файл маленький, вызываем обычную сортировку,
        // в противном случае внешнюю
        File file = new File("File/BigFile.txt");
        if(file.length() < 1024 * 1024){
            try {
                ArrayList<String> splitString = new ArrayList<>(Files.readAllLines(Paths.get(String.valueOf(file))));
                // Сортируем ArrayList
                Collections.sort(splitString);
                FileWriter writer = new FileWriter("File/SortBigFile.txt");
                // Записываем отсортированные данные в файл (SortBigFile)
                for(String str: splitString){
                    writer.write(str + System.lineSeparator());
                }
                writer.close();
                System.out.println("Сортировка завершена");
            }
            catch (IOException e) {
                System.out.println(e.getMessage());
            }
        }else{
            // Делим файл на файлы поменьше и возвращаем путь до директории с файлами
            var pathDirectorySplit = SplitFiles(pathDB);
            // Сортируем маленькие файлы
            sortSplitFiles(pathDirectorySplit);
            // Объединяем маленькие отсортированные файлы
            mergeSortSplitFiles(pathDirectorySplit, pathDB);
        }
    }

    private static void mergeSortSplitFiles(String pathDirectorySplit, String pathDB) {
        // Путь к файлу
        var pathToFile = String.format("%s/SortBigFile.txt", pathDB);

        try {
            // Удаляем старый файл (SortBigFile.txt), если он существует
            Files.delete(Paths.get(pathToFile));

            // Получаем массив путей к маленьким файлам
            File folderSplitFiles = new File(pathDirectorySplit);
            File[] listOfSplitFiles = folderSplitFiles.listFiles();

            // Количество маленьких файлов
            var lengthSplitFiles = listOfSplitFiles != null ? listOfSplitFiles.length : 0;

            // Создаем ArrayList, в который будут записаны данные
            // из другого ArrayList
            ArrayList<ArrayList<String>> nodes = new ArrayList<ArrayList<String>>();

            // Максимальный размер очереди
            var maxSizeQueue = maxLengthSplitFile / lengthSplitFiles + 2;

            // Если файл один, копируем и переименовываем его
            if(lengthSplitFiles < 2){
                Files.move(Paths.get(String.valueOf(listOfSplitFiles[0])), Paths.get(pathToFile));
                System.out.println("Файл успешно перемещен!");
                return;
            }

            // Добавляем в массив файлы, которые необходимо прочитать
            FileReader[] fileReaders = new FileReader[lengthSplitFiles];
            for (int i = 0; i < lengthSplitFiles; i++){
                fileReaders[i] = new FileReader(listOfSplitFiles[i]);
            }


            // Записываем в массив ArrayList часть данных из файлов
            for (var i = 0; i < lengthSplitFiles; i++){
               nodes.add(i, ListSplitFile(fileReaders[i], maxSizeQueue));
            }

            // Создаем отсортированный файл и в цикле его заполняем
            FileWriter sortFile = new FileWriter(pathToFile);
            while(true){
                // Находим минимальный элемент и индекс среди первых
                var min = "";
                var minIndex = -1;
                for (var i = 0; i < lengthSplitFiles; i++){
                    if(nodes.get(i) != null){
                        if(nodes.get(i).size() == 0)
                            nodes.add(i, ListSplitFile(fileReaders[i], maxSizeQueue));
                        if(nodes.get(i).size() == 0)
                            continue;
                        if (minIndex < 0 || min.compareTo(nodes.get(0).get(0)) > 0){
                            min = nodes.get(i).get(0);
                            minIndex = i;
                        }
                    }
                }
                if (minIndex == -1)
                    break;
                // Записываем в файл и изымаем элемент из очереди
                sortFile.write(nodes.get(minIndex).get(0));
                sortFile.write("\n");
                nodes.get(minIndex).remove(0);
            }
            sortFile.close();

            // Закрываем файлы для чтения и удаляем лишнее
            for (int i = 0; i < lengthSplitFiles; i++){
                fileReaders[i].close();
                Files.delete(Paths.get(String.valueOf(listOfSplitFiles[i])));
            }
            System.out.println("Ура! Сортировка завершена!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Добавляем в очередь из одного маленького файла часть строк
    private static ArrayList<String> ListSplitFile(FileReader fileReader, int maxSizeQueue) throws IOException {
        var listSplitFile = new ArrayList<String>();

        String line;
        var lengthLine = 0;
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        while (maxSizeQueue >= lengthLine && (line = bufferedReader.readLine()) != null){
            lengthLine += line.length();
            listSplitFile.add(line);
        }

        return listSplitFile;
    }

    // Данный метод сортирует маленькие файлы
    private static void sortSplitFiles(String pathDirectorySplit) {

         File folder = new File(pathDirectorySplit);
         // Метод listFiles() - чтение массива файлов и подкаталогов
         File[] listOfFiles = folder.listFiles();

         int numberSortedFiles = 0;
         // Не забываем увеличить счетчик
         String sortedFileName = String.format("File/FileTemp/Sorted_%d.txt", numberSortedFiles);

        // Проходимся по всем маленьким файлам
         for (File file : listOfFiles){
             if(file.isFile()){
                 try {
                     // Записываем все строки из файла в массив
                     ArrayList<String> list = new ArrayList<>(Files.readAllLines(Paths.get(file.getAbsolutePath())));

                     // Сортируем строки в массиве
                     Collections.sort(list);

                     FileWriter writer = new FileWriter(sortedFileName);
                        for(String str : list){
                            writer.write(str + System.lineSeparator());
                        }
                        writer.close();

                        Files.delete(Paths.get(file.getAbsolutePath()));
                        numberSortedFiles++;
                        sortedFileName = String.format("File/FileTemp/Sorted_%d.txt", numberSortedFiles);

                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         }
        System.out.println("Маленькие файлы отсортированы");
    }

    private static String SplitFiles(String pathDB) {
        // Нумерация разделенных файлов (split files)
        int splitFileNumber = 0;

        // Путь к директории с маленькими файлами
        var pathDirectorySplit = "File/FileTemp";

        // Создание директории, содержащей файлы меньшего размера
        File directory = new File(pathDirectorySplit);
        boolean successAddDirectory = directory.mkdir();
        if(successAddDirectory){
            System.out.println("Директория FileTemp успешно создана!");
        }else{
            System.out.println("Ошибка при создании директории!");
        }

        try {
            // Создаем файл поменьше и начинаем в него запись
            FileWriter writerSplitFile = new FileWriter("File/FileTemp/SplitFile_0.txt", true);

            // Открываем большой файл (File/BigFile.txt) для чтения
            File file = new File("File/BigFile.txt");
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);

            String formattedFileNameSplit = String.format("File/FileTemp/SplitFile_%d.txt", splitFileNumber);

            // Проходимся по всем строкам большого файла
            String line;
            while((line = br.readLine()) != null){
                System.out.println("Делим файл на маленькие файлы...");
                // Копируем строку из большога файла в маленький
                writerSplitFile.write(line);
                writerSplitFile.write("\n");

                // Создаем новый маленький файл, если длина
                // предыдущего маленького файла превысила установленное значение
                File fileSize = new File(formattedFileNameSplit);
                if(fileSize.length() > maxLengthSplitFile && !line.equals("")){
                    writerSplitFile.close();
                    splitFileNumber++;

                    // Записываем путь к новому файлу
                    String strTemp = Integer.toString(splitFileNumber);
                    formattedFileNameSplit = String.format("File/FileTemp/SplitFile_%s.txt", strTemp);
                    writerSplitFile = new FileWriter(formattedFileNameSplit);
                }
            }
            // Не забываем закрыть br, fr!
            writerSplitFile.close();
            br.close();
            fr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println();
        return pathDirectorySplit;
    }

    private static String GeneratorBigFile(){
        // Путь к директории
        var pathDirectory = "File";
        // Путь к файлу
        var pathFile = "File/BigFile.txt";

        // Преобразуем строку в новый массив символов
        char[] letters = "abcdefghijklmnopqrstuvwxyz0123456789".toCharArray();

        // Получение и проверка начальных данных
        Scanner scanner = new Scanner(System.in);
        System.out.println("Генерируем файл для сортировки. \nВведите количество строк:");
        int countOfLinesEntered = trueInputInt();
        System.out.println("Введите максимальную длину строки:");
        int maxLengthString = trueInputInt();
        scanner.close();

        // Генератор случайных чисел
        Random random = new Random();

        // Создание директории
        File directory = new File(pathDirectory);
        boolean successAddDirectory = directory.mkdir();
        if(successAddDirectory){
            System.out.println("Директория успешно создана");
        }else{
            System.out.println("Ошибка при создании");
        }

        // Удаление файла, если есть
        File file = new File(pathFile);
        if(file.delete()){
            System.out.println("Файл удален");
        }else{
            System.out.println("Файл успешно создан");
        }

        // Запись данных в файл
        try  (FileWriter writer = new FileWriter(pathFile, true)){
            // Цикл для строк
            for (var i=0; i < countOfLinesEntered; i++){
                // Прогресс выполнения генерации
                System.out.printf("Генерация файла: %.2f%%\r", 100.0 * i / (countOfLinesEntered-1));

                // Случайная длина строки
                int min = 1;
                int diff = maxLengthString - min;
                int lengthString = random.nextInt(diff + 1) + min;

                // Создаем пустое слово
                StringBuilder word = new StringBuilder("");
                for(int j=1; j <= lengthString; j++){
                    // Случайный символ
                    int letterNum = random.nextInt(letters.length - 1);
                    // Добавляем символ в строку
                    word.append(letters[letterNum]);
                }
                // Запись в файл
                writer.write(String.valueOf(word));
                writer.write("\n");
                System.out.println();
            }

        }catch (IOException ex){
            System.out.println(ex.getMessage());
        }

        System.out.println();
        return pathDirectory;
    }

    // Данный метод проверяет, что введено целое число
    private static int trueInputInt(){
       Scanner scanner = new Scanner(System.in);
        while(!scanner.hasNextInt()){
            System.out.println("Вы ввели не целое число, повторите ввод");
            scanner.next();
        }
        int number = scanner.nextInt();
        while(number <= 0){
            System.out.println("Введенное число должно быть больше нуля, повторите ввод");
            while (!scanner.hasNextInt()){
                System.out.println("Неправильно! Повторите ввод");
                scanner.next();
            }
            number = scanner.nextInt();
        }
        return number;
    }
}
