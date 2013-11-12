using System;
using System.Runtime.Serialization;

namespace FindMyBuddies.Api.Models
{
    [DataContract]
    public class FriendRequestResponseModel
    {
        [DataMember(Name = "fromUserId")]
        public int FromUserId { get; set; }

        [DataMember(Name = "fromUserNickname")]
        public string FromUserNickname { get; set; }

        [DataMember(Name = "isAccepted")]
        public bool IsAccepted { get; set; }

        [DataMember(Name = "isLeftForLater")]
        public bool IsLeftForLater { get; set; }
    }
}