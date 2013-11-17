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
        public string ThumbUrl { get; set; }

        [Required]
        public String ImageDateAsString { get; set; }

        [Required]
        public string TimestampDifferenceWithCoordinates { get; set; }

        [Required]
        public string CoordinatesAccuracy { get; set; }

        public virtual Coordinates Coordinates { get; set; }

        public virtual User User { get; set; }
    }
}
