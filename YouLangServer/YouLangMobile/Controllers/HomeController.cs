using System;
using System.Collections.Generic;
using System.Configuration;
using System.Linq;
using System.Web;
using System.Web.Mvc;
using System.Data;
using System.Data.SqlClient;
using Newtonsoft.Json;
using YouLangMobile.API;
using System.Net.Mail;
using System.Net;

namespace YouLangMobile.Controllers
{
    public class HomeController : Controller
    {
        private Dictionary<string, string> db = new Dictionary<string, string>();

        // GET: Home
        public System.Web.Mvc.ActionResult Index()
        {
            return View();
        }

        [HttpGet]
        public string Login()
        {
            return "Hello!";
        }

        [HttpPost]
        public string Login(string credentials)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            var user = JsonConvert.DeserializeObject<Credentials>(credentials);

            try
            {
                SqlDataReader sqlReader = null;

                SqlCommand command = new SqlCommand("SELECT [name], [password] FROM [users]", sqlConnection);

                db.Clear();

                sqlReader = command.ExecuteReader();

                while (sqlReader.Read())
                {
                    db.Add(Convert.ToString(sqlReader["name"]), Convert.ToString(sqlReader["password"]));
                }

                sqlReader.Close();

                if (user.Password == db[user.Login])
                {
                    SqlCommand getConf = new SqlCommand("SELECT [email_verified_at] FROM [users] WHERE [name]=@Login", sqlConnection);
                    getConf.Parameters.AddWithValue("Login", user.Login);

                    int email_verified_at = Convert.ToInt32(getConf.ExecuteScalar());

                    if (email_verified_at == 1)
                    {
                        if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                            sqlConnection.Close();

                        return "ok";
                    }
                    else
                    {
                        if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                            sqlConnection.Close();

                        return "Page is not verified!";
                    }
                }
                else
                {
                    if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                        sqlConnection.Close();

                    return "Incorrect login or password!";
                }
            }
            catch (Exception)
            {
                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return "Incorrect data!";
            }
        }

        [HttpPost]
        public string RegistreUser(string fullCredentials)
        {
            FullCredentials fC = JsonConvert.DeserializeObject<FullCredentials>(fullCredentials);

            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            List<string> logins = new List<string>();

            SqlDataReader sqlReader = null;

            try
            {
                SqlCommand readCommand = new SqlCommand("SELECT [name] FROM [users]", sqlConnection);

                sqlReader = readCommand.ExecuteReader();

                while (sqlReader.Read())
                {
                    logins.Add(Convert.ToString(sqlReader["name"]));
                }
            }
            catch (Exception)
            {

            }
            finally
            {
                if (sqlReader != null)
                    sqlReader.Close();
            }

            if (!logins.Contains(fC.Login))
            {
                SqlCommand command = new SqlCommand("INSERT INTO Users (name, email, email_verified_at, password, isAdmin, created_at)VALUES(@name, @email, @email_verified_at, @password, @isAdmin, @created_at);", sqlConnection);

                command.Parameters.AddWithValue("name", fC.Login);
                command.Parameters.AddWithValue("email", fC.Email);
                command.Parameters.AddWithValue("email_verified_at", 0);
                command.Parameters.AddWithValue("password", fC.Password);
                command.Parameters.AddWithValue("isAdmin", 0);
                command.Parameters.AddWithValue("created_at", Convert.ToDateTime(DateTime.Now));

                command.ExecuteNonQuery();

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(API.ActionResult.Success);
            }

            if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                sqlConnection.Close();

            return JsonConvert.SerializeObject(API.ActionResult.Failed);
        }

        [HttpPost]
        public string SendConfirmingLetter(string confirmingCredentials)
        {
            ConfirmingCredentials cC = JsonConvert.DeserializeObject<ConfirmingCredentials>(confirmingCredentials);

            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            string code = Guid.NewGuid().ToString();

            int c = 0;

            SqlCommand command1 = new SqlCommand("UPDATE [users] SET [request_code]=@request_code WHERE [name]=@Login", sqlConnection);
            command1.Parameters.AddWithValue("Login", cC.Login);
            command1.Parameters.AddWithValue("request_code", code);

            c += command1.ExecuteNonQuery();

            #region Send a Letter With Confirming Link

            string email = "youlangmobile@gmail.com";
            string password = "meinland@2019";
            string toEmail = cC.Email;
            try
            {
                //Указываем SMTP сервер и авторизуемся.
                SmtpClient Smtp_Client = new SmtpClient("smtp.gmail.com", 587);
                Smtp_Client.Credentials = new NetworkCredential(email, password);
                //Выключаем или включаем
                Smtp_Client.EnableSsl = true;

                //Приступаем к формированию самого письма
                MailMessage Message = new MailMessage();
                Message.From = new MailAddress(email); // от кого
                Message.To.Add(new MailAddress(toEmail)); // кому
                Message.Subject = "Account Verification";
                Message.IsBodyHtml = true;

                Message.Body = $@"<div><table><tr><td><img src=""http://youlangmobile-001-site1.gtempurl.com/logo.jpg"" width=""60"" height=""60"" /></td>
<td><h3>Congratulations on your registration!</h3></td></tr>
<tr><td colspan=""2""><p>Hello, {cC.Login}!</p>
<p>We congratulate you on registering!</p>
To confirm your account please follow the link: <a href=""http://youlangmobile-001-site1.gtempurl.com/Home/EmailConfirming?code={code}&login={cC.Login}"">Verify account</a>
</td></tr></table></div>"; // текст письма

                Smtp_Client.Send(Message); //непосредственно само отправление

                c++;
            }
            catch (Exception)
            {

            }

            #endregion

            if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                sqlConnection.Close();

            if (c / 2 == 1)
                return JsonConvert.SerializeObject(API.ActionResult.Success);

            return JsonConvert.SerializeObject(API.ActionResult.Failed);
        }

        [HttpGet]
        public string EmailConfirming(string code, string login)
        {
            string respCode = code;

            string usersLogin = login;

            if (!string.IsNullOrEmpty(respCode) && !string.IsNullOrEmpty(usersLogin))
            {
                string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
                SqlConnection sqlConnection = new SqlConnection(connectionString);
                sqlConnection.Open();

                SqlCommand getConfStatus = new SqlCommand("SELECT [email_verified_at] FROM [users] WHERE [name]=@Login", sqlConnection);
                getConfStatus.Parameters.AddWithValue("Login", usersLogin);
                int email_verified_at = Convert.ToInt32(getConfStatus.ExecuteScalar());

                if (email_verified_at == 0)
                {
                    SqlCommand command = new SqlCommand("SELECT [request_code] FROM [users] WHERE [name]=@Login", sqlConnection);
                    command.Parameters.AddWithValue("Login", usersLogin);
                    string reqCode = Convert.ToString(command.ExecuteScalar());

                    if (reqCode == respCode)
                    {
                        SqlCommand command1 = new SqlCommand("UPDATE [users] SET [email_verified_at]=@email_verified_at, [response_code]=@response_code WHERE [name]=@Login", sqlConnection);
                        command1.Parameters.AddWithValue("Login", usersLogin);
                        command1.Parameters.AddWithValue("response_code", respCode);
                        command1.Parameters.AddWithValue("email_verified_at", 1);

                        command1.ExecuteNonQuery();


                        if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                            sqlConnection.Close();

                        return "Account was successfully verified! Sign in app!";
                    }
                    else
                    {
                        if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                            sqlConnection.Close();

                        return "Invalid confirmation code!";
                    }
                }
                else
                {
                    if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                        sqlConnection.Close();

                    return "Account has been already confirmed! Sign in app!";
                }
            }
            else
            {
                return "Incorrect data!";
            }
        }

        [HttpPost]
        public string CheckExistence(string login)
        {
            string crrUser = JsonConvert.DeserializeObject<string>(login);

            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            SqlDataReader sqlReader = null;

            SqlCommand command = new SqlCommand("SELECT [name] FROM [users]", sqlConnection);

            List<string> users = new List<string>();

            try
            {
                sqlReader = command.ExecuteReader();

                while (sqlReader.Read())
                {
                    users.Add(Convert.ToString(sqlReader["name"]));
                }
            }
            catch (Exception)
            {

            }
            finally
            {
                if (sqlReader != null)
                    sqlReader.Close();
            }

            if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                sqlConnection.Close();

            if (!users.Contains(crrUser))
                return JsonConvert.SerializeObject(API.ActionResult.Success);

            return JsonConvert.SerializeObject(API.ActionResult.Failed);
        }

        [HttpPost]
        public string GetVocabulary(string login)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            List<VocabularyItem> list = new List<VocabularyItem>();

            string crrUser = JsonConvert.DeserializeObject<string>(login);



            int wordId = 0;

            int userId = 0;

            SqlCommand getUserId = new SqlCommand("SELECT [Id] FROM [users] WHERE [name]=@login", sqlConnection);

            getUserId.Parameters.AddWithValue("login", crrUser);

            userId = Convert.ToInt32(getUserId.ExecuteScalar());

            string log = "";

            try
            {
                SqlCommand isInUsersVocCommand = new SqlCommand("SELECT [word_id] FROM [uservocabulary] WHERE [user_id]=@userId", sqlConnection);

                isInUsersVocCommand.Parameters.AddWithValue("userId", Convert.ToInt32(userId));

                SqlDataReader reader1 = isInUsersVocCommand.ExecuteReader();

                List<int> wordIdsList = new List<int>();

                while (reader1.Read())
                {
                    wordIdsList.Add(Convert.ToInt32(reader1["word_id"]));
                }

                reader1.Close();


                SqlCommand getWords = new SqlCommand("SELECT * FROM [vocabulary]", sqlConnection);

                SqlDataReader reader = getWords.ExecuteReader();

                while (reader.Read())
                {
                    VocabularyItem item = new VocabularyItem();

                    item.IsInUsersVoc = false;
                    item.Word = Convert.ToString(reader["word"]);
                    item.Translate = Convert.ToString(reader["translate"]);
                    item.WordId = Convert.ToString(reader["Id"]);

                    if (wordIdsList.Contains(Convert.ToInt32(reader["Id"])))
                    {
                        item.IsInUsersVoc = true;

                        log += Convert.ToString(wordId) + ", ";
                    }

                    list.Add(item);
                }

                if (reader != null)
                    reader.Close();

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(list);
            }
            catch (Exception ex) { return JsonConvert.SerializeObject(log + " " + ex.Message + "\n" + ex.StackTrace); }

            //return JsonConvert.SerializeObject(API.ActionResult.Failed);
        }

        [HttpPost]
        public string GetUserVocabulary(string login)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            List<VocabularyItem> list = new List<VocabularyItem>();

            string crrUser = JsonConvert.DeserializeObject<string>(login);

            int userId = 0;

            SqlCommand getUserId = new SqlCommand("SELECT [Id] FROM [users] WHERE [name]=@login", sqlConnection);

            getUserId.Parameters.AddWithValue("login", crrUser);

            userId = Convert.ToInt32(getUserId.ExecuteScalar());

            try
            {
                SqlCommand getWords = new SqlCommand("SELECT * FROM [uservocabulary] WHERE [user_id]=@userId", sqlConnection);

                getWords.Parameters.AddWithValue("userId", userId);

                SqlDataReader reader = getWords.ExecuteReader();

                while (reader.Read())
                {
                    VocabularyItem item = new VocabularyItem();

                    item.WordId = Convert.ToString(reader["word_id"]);

                    SqlCommand getWord = new SqlCommand("SELECT * FROM [vocabulary] WHERE [Id]=@wordId", sqlConnection);

                    getWord.Parameters.AddWithValue("wordId", item.WordId);

                    SqlDataReader wordReader = getWord.ExecuteReader();

                    while (wordReader.Read())
                    {
                        item.Word = Convert.ToString(wordReader["word"]);

                        item.Translate = Convert.ToString(wordReader["translate"]);
                    }

                    if (wordReader != null)
                        wordReader.Close();

                    item.IsInUsersVoc = true;

                    list.Add(item);
                }

                if (reader != null)
                    reader.Close();

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(list);
            }
            catch (Exception ex) { return JsonConvert.SerializeObject(ex.Message + "\n" + ex.StackTrace); }

            //return JsonConvert.SerializeObject(API.ActionResult.Failed);
        }

        [HttpPost]
        public string AddWord(string addingParams)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            WordAddingParams wP = JsonConvert.DeserializeObject<WordAddingParams>(addingParams);

            string userLogin = wP.Login;
            int wordId = Convert.ToInt32(wP.WordId);

            int userId = 0;

            try
            {

                SqlCommand getUserId = new SqlCommand("SELECT [Id] FROM [users] WHERE [name]=@login", sqlConnection);

                getUserId.Parameters.AddWithValue("login", userLogin);

                userId = Convert.ToInt32(getUserId.ExecuteScalar());

                int result = 0;

                AddWordActionResult wordActionResult = AddWordActionResult.Failed;

                if (wP.Add)
                {
                    SqlCommand addCommand = new SqlCommand("INSERT INTO [uservocabulary] (user_id, word_id)VALUES(@user_id, @word_id);", sqlConnection);

                    addCommand.Parameters.AddWithValue("user_id", userId);
                    addCommand.Parameters.AddWithValue("word_id", wordId);

                    result = Convert.ToInt32(addCommand.ExecuteScalar());

                    wordActionResult = AddWordActionResult.Added;
                }
                else
                {
                    SqlCommand deleteCommand = new SqlCommand("DELETE FROM [uservocabulary] WHERE [user_id]=@user_id AND [word_id]=@word_id", sqlConnection);

                    deleteCommand.Parameters.AddWithValue("user_id", userId);
                    deleteCommand.Parameters.AddWithValue("word_id", wordId);

                    result = Convert.ToInt32(deleteCommand.ExecuteScalar());

                    wordActionResult = AddWordActionResult.Deleted;
                }

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(wordActionResult);
            }
            catch (Exception ex)
            {
                return JsonConvert.SerializeObject(ex.Message + "---" + ex.StackTrace);
            }
        }

        [HttpPost]
        public string GetDetailedWord(string wordRequest)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            DetailedWordRequest request = JsonConvert.DeserializeObject<DetailedWordRequest>(wordRequest);

            DetailedWord detailedWord = new DetailedWord();

            try
            {
                int userId = 0;

                SqlCommand getUserId = new SqlCommand("SELECT [Id] FROM [users] WHERE [name]=@login", sqlConnection);

                getUserId.Parameters.AddWithValue("login", request.Login);

                userId = Convert.ToInt32(getUserId.ExecuteScalar());


                SqlCommand isInUsersVocCommand = new SqlCommand("SELECT [word_id] FROM [uservocabulary] WHERE [user_id]=@userId", sqlConnection);

                isInUsersVocCommand.Parameters.AddWithValue("userId", Convert.ToInt32(userId));

                SqlDataReader reader1 = isInUsersVocCommand.ExecuteReader();

                List<int> wordIdsList = new List<int>();

                while (reader1.Read())
                {
                    wordIdsList.Add(Convert.ToInt32(reader1["word_id"]));
                }

                reader1.Close();



                SqlCommand getWord = new SqlCommand("SELECT * FROM [vocabulary] WHERE [Id]=@wordId", sqlConnection);

                getWord.Parameters.AddWithValue("wordId", request.WordId);

                SqlDataReader wordReader = getWord.ExecuteReader();

                while (wordReader.Read())
                {
                    detailedWord.IsInUsersVoc = false;

                    detailedWord.Word = Convert.ToString(wordReader["word"]);

                    detailedWord.Translate = Convert.ToString(wordReader["translate"]);

                    detailedWord.Transcription = Convert.ToString(wordReader["transcription"]);

                    detailedWord.Description = Convert.ToString(wordReader["description"]);

                    detailedWord.PartOfSpeech = Convert.ToString(wordReader["parts_of_speech"]);

                    if (wordIdsList.Contains(Convert.ToInt32(wordReader["Id"])))
                    {
                        detailedWord.IsInUsersVoc = true;
                    }
                }

                if (wordReader != null)
                    wordReader.Close();

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(detailedWord);
            }
            catch (Exception ex)
            {
                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(ex.Message + "---" + ex.StackTrace);
            }
        }

        [HttpPost]
        public string GetProfile(string login)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            string crrUser = JsonConvert.DeserializeObject<string>(login);

            ProfileInfo profileInfo = new ProfileInfo();

            try
            {
                SqlCommand getProfileInfoCommand = new SqlCommand("SELECT [Id], [name], [email] FROM [users] WHERE [name]=@login", sqlConnection);

                getProfileInfoCommand.Parameters.AddWithValue("login", crrUser);

                SqlDataReader userReader = getProfileInfoCommand.ExecuteReader();

                while (userReader.Read())
                {
                    profileInfo.Id = Convert.ToString(userReader["Id"]);
                    profileInfo.Login = Convert.ToString(userReader["name"]);
                    profileInfo.Email = Convert.ToString(userReader["email"]);
                }

                if (userReader != null)
                    userReader.Close();

                SqlCommand getUserVocWordsCountCommand = new SqlCommand("SELECT count(*) FROM [uservocabulary] WHERE [user_id]=@userId", sqlConnection);

                getUserVocWordsCountCommand.Parameters.AddWithValue("userId", profileInfo.Id);

                profileInfo.WordsInVocabulary = Convert.ToString(getUserVocWordsCountCommand.ExecuteScalar());

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(profileInfo);
            }
            catch (Exception ex)
            {

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();


                return JsonConvert.SerializeObject(ex.Message + "---" + ex.StackTrace);
            }
        }

        [HttpPost]
        public string GenerateTranslationTest(string login)
        {
            string connectionString = ConfigurationManager.ConnectionStrings["ulangdb"].ConnectionString;
            SqlConnection sqlConnection = new SqlConnection(connectionString);
            sqlConnection.Open();

            string crrUser = JsonConvert.DeserializeObject<string>(login);

            List<int> possibleIds = new List<int>();
            List<int> listNumbers = new List<int>();

            try
            {
                SqlCommand getPossibleIds = new SqlCommand("SELECT [Id], [word], [translate] FROM [vocabulary]", sqlConnection);

                Dictionary<int, DetailedWord> dWList = new Dictionary<int, DetailedWord>();

                SqlDataReader wordReader = getPossibleIds.ExecuteReader();

                while (wordReader.Read())
                {
                    possibleIds.Add(Convert.ToInt32(wordReader["Id"]));

                    dWList.Add(Convert.ToInt32(wordReader["Id"]), new DetailedWord()
                    {
                        Word = Convert.ToString(wordReader["word"]),

                        Translate = Convert.ToString(wordReader["translate"])
                    });
                }

                if (wordReader != null)
                    wordReader.Close();

                TranslationTest translationTest = new TranslationTest();

                translationTest.Tasks = new List<TranslationTask>();

                possibleIds.Sort();

                for (int i = 0; i < 5; i++)
                {
                    int index = new Random().Next(0, possibleIds.Count);
                    listNumbers.Add(possibleIds[index]);
                    possibleIds.RemoveAt(index);
                }

                translationTest.TaskCount = listNumbers.Count;

                for (int i = 0; i < listNumbers.Count; i++)
                {
                    TranslationTask translationTask = new TranslationTask();

                    translationTask.Word = dWList[listNumbers[i]].Word;

                    translationTask.Translates = new List<string>();

                    Random r = new Random();

                    int rrr = 0;

                    rrr = r.Next(0, 2);

                    switch (rrr)
                    {
                        case 0:

                            translationTask.Translates.Add(dWList[listNumbers[i]].Translate);
                            translationTask.RightTranslateIndex = 0;

                            List<int> tmp = listNumbers.ToList();
                            tmp.Remove(listNumbers[i]);
                            translationTask.Translates.Add(dWList[tmp[new Random().Next(0, tmp.Count)]].Translate);

                            break;
                        case 1:

                            List<int> tmp1 = listNumbers.ToList();
                            tmp1.Remove(listNumbers[i]);
                            translationTask.Translates.Add(dWList[tmp1[new Random().Next(0, tmp1.Count)]].Translate);

                            translationTask.Translates.Add(dWList[listNumbers[i]].Translate);
                            translationTask.RightTranslateIndex = 1;

                            break;
                    }

                    translationTest.Tasks.Add(translationTask);
                }

                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(translationTest);
            }
            catch (Exception ex)
            {
                if (sqlConnection != null && sqlConnection.State != ConnectionState.Closed)
                    sqlConnection.Close();

                return JsonConvert.SerializeObject(ex.Message + "---" + ex.StackTrace);
            }
        }
    }
}