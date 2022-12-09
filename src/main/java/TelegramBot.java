import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.telegram.telegrambots.ApiContextInitializer;
import org.telegram.telegrambots.TelegramBotsApi;
import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Message;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;
import org.telegram.telegrambots.exceptions.TelegramApiRequestException;
import java.io.IOException;

//запуск телеграмм-бота
public class TelegramBot extends TelegramLongPollingBot {
    public static void main(String[] args) {
        ApiContextInitializer.init();
        TelegramBotsApi telegramBotsApi = new TelegramBotsApi();
        try {
            telegramBotsApi.registerBot(new TelegramBot());
        }
        catch (TelegramApiRequestException e) {
            e.printStackTrace();
        }

        //распознавание лиц по фото
        System.loadLibrary(Core.NATIVE_LIBRARY_NAME);

        //путь к картинке
        String imgFile = "images/2.jpg";
        Mat src = Imgcodecs.imread(imgFile);

        //загрузка классификатора xml из библиотеки Open CV
        String xmlFile = "xml/lbpcascade_frontalface.xml";
        CascadeClassifier cc = new CascadeClassifier(xmlFile);

        MatOfRect faceDetection = new MatOfRect();
        cc.detectMultiScale(src, faceDetection);
        System.out.println(String.format("Detected faces: %d", faceDetection.toArray().length));

        for(Rect rect: faceDetection.toArray()) {
            Imgproc.rectangle(src, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height), new Scalar(0, 0, 255), 3);
        }

        //результат распознавания возвращает в папку images
        Imgcodecs.imwrite("images/2_out.jpg", src);
        System.out.println("Image Detection Finished");
    }

    //название телеграмм-бота
    public String getBotUsername() {
        return "TB";
    }

    //токен канала телеграмм-бота
    public String getBotToken() {
        return "5846769782:AAHTBRm4dIzvYCvZBNTv46KKoW5YyDEoAgc";
    }


    public void sendMsg(Message message, String text) {
        SendMessage sendMessage = new SendMessage();
        sendMessage.enableMarkdown(true);

        sendMessage.setChatId(message.getChatId().toString());
        sendMessage.setReplyToMessageId(message.getMessageId());
        sendMessage.setText(text);

        try {
            new SetButtons(sendMessage);
            sendMessage(sendMessage);
        }
        catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    //использование команд
    @Override
    public void onUpdateReceived(Update update) {
        Model model = new Model();
        Message message = update.getMessage();
        if (message != null && message.hasText()) {
            switch (message.getText()) {
                case "/start":
                    sendMsg(message, "Привет!");
                    break;
                case "/help":
                    sendMsg(message, "Вы можете ввести команду или выбрать внизу три кнопки:\n\n"+
                            "Введите /start, чтобы увидеть приветственное сообщение\n"+
                            "Введите /help, чтобы снова увидеть это сообщение\n"+
                            "Введите слово /weather и затем город, чтобы узнать погоду");
                    break;
                case "/weather":
                    sendMsg(message, "Введите название города и покажу погоду");
                    break;
                default:
                    try {
                        sendMsg(message, Weather.getWeather(message.getText(), model));
                    }
                    catch (IOException e) {
                        sendMsg(message, "Извините, данная фукнция недоступна!");
                    }
            }
        }
    }
}