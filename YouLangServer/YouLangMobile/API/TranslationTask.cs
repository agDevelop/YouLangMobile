using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace YouLangMobile.API
{
    public class TranslationTask
    {
        public string Word { get; set; } = "";

        public List<string> Translates { get; set; } = null;

        public int RightTranslateIndex { get; set; } = 0;
    }
}