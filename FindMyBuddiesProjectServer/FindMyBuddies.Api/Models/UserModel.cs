using System;
using System.Runtime.Serialization;

namespace FindMyBuddies.Api.Models
{
    [DataContract]
    public class UserModel
    {
        [DataMember(Name = "username")]
        public string Username { get; set; }

        [DataMember(Name = "nickname")]
        public string Nickname { get; set; }

        [DataMember(Name = "authCode")]
        public string AuthCode { get; set; }
    }
}