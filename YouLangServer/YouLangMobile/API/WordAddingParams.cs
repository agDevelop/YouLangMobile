using System;
using System.Collections.Generic;
using System.Linq;
using System.Web;

namespace YouLangMobile.API
{
    public class WordAddingParams
    {
        public string WordId { get; set; }

        public string Login { get; set; }

        public bool Add { get; set; }
    }
}