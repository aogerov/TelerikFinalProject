using System;
using System.ComponentModel.DataAnnotations;

namespace FindMyBuddies.Model
{
    public class Image
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public string Url { get; set; }

        [Required]
        public DateTime Timestamp { get; set; }

        public virtual Coordinates Coordinates { get; set; }

        public virtual User User { get; set; }
    }
}
