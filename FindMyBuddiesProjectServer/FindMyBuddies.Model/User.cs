using System;
using System.Collections.Generic;
using System.ComponentModel.DataAnnotations;

namespace FindMyBuddies.Model
{
    public class User
    {
        public User()
        {
            this.Friends = new HashSet<User>();
            this.FriendRequests = new HashSet<FriendRequest>();
        }

        [Key]
        public int Id { get; set; }

        [Required]
        [MinLength(6), MaxLength(30)]
        public string Username { get; set; }

        [Required]
        [MinLength(6), MaxLength(30)]
        public string Nickname { get; set; }

        [Required]
        public string AuthCode { get; set; }

        public string SessionKey { get; set; }

        public bool IsOnline { get; set; }

        public virtual Coordinates Coordinates { get; set; }

        public virtual ICollection<Image> Images { get; set; }

        public virtual ICollection<User> Friends { get; set; }

        public virtual ICollection<FriendRequest> FriendRequests { get; set; }
    }
}