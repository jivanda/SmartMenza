using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using Microsoft.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore.Design;

namespace SmartMenza.Data.Context
{
    public class SmartMenzaContextFactory : IDesignTimeDbContextFactory<SmartMenzaContext>
    {
        public SmartMenzaContext CreateDbContext(string[] args)
        {
            var optionsBuilder = new DbContextOptionsBuilder<SmartMenzaContext>();
            optionsBuilder.UseSqlServer("Server=(localdb)\\MSSQLLocalDB;Database=SmartMenzaDB;Trusted_Connection=True;MultipleActiveResultSets=true");

            return new SmartMenzaContext(optionsBuilder.Options);
        }
    }
}