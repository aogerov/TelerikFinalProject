using System;
using System.ComponentModel.DataAnnotations;

namespace FindMyBuddies.Model
{
    public class FriendRequest
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public int FromUserId { get; set; }

        [Required]
        public string FromUserNickname { get; set; }

        [Required]
        public bool IsShowed { get; set; }

        public virtual User User { get; set; }
    }
}
