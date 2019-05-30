using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace YouLangMobile.API
{
    public class VocabularyItem
    {
        public string WordId { get; set; }

        public string Word { get; set; }

        public string Translate { get; set; }

        public bool IsInUsersVoc { get; set; }
    }
}