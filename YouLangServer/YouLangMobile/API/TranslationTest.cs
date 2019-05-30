using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace YouLangMobile.API
{
    public class TranslationTest
    {
        public int TaskCount { get; set; } = 0;

        public List<TranslationTask> Tasks { get; set; } = null;
    }
}