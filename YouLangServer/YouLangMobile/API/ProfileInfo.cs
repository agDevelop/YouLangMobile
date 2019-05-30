using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace YouLangMobile.API
{
    public class ProfileInfo
    {
        public string Id { get; set; }

        public string Login { get; set; }

        public string Email { get; set; }

        public string WordsInVocabulary { get; set; }
    }
}