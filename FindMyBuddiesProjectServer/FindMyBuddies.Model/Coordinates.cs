﻿using System;
using System.ComponentModel.DataAnnotations;

namespace FindMyBuddies.Model
{
    public class Coordinates
    {
        [Key]
        public int Id { get; set; }

        [Required]
        public string Latitude { get; set; }

        [Required]
        public string Longitude { get; set; }

        [Required]
        public DateTime Timestamp { get; set; }
    }
}
