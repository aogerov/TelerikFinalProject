using System;
using System.Runtime.Serialization;

namespace FindMyBuddies.Api.Models
{
    [DataContract]
    public class UserLoggedModel
    {
        [DataMember(Name = "nickname")]
        public string Nickname { get; set; }

        [DataMember(Name = "sessionKey")]
        public string SessionKey { get; set; }
    }
}