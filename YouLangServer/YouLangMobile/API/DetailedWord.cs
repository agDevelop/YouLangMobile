using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace YouLangMobile.API
{
    public class DetailedWord
    {
        public string Word { get; set; }

        public string Translate { get; set; }

        public string Transcription { get; set; }

        public string PartOfSpeech { get; set; }

        public string Image { get; set; }

        public string RootWordId { get; set; }

        public string Description { get; set; }

        public string Tags { get; set; }

        public bool IsInUsersVoc { get; set; }

        public string Login { get; set; }
    }
}