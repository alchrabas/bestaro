const fs = require('fs');

/** Split a domain name into its subdomain and parent domain names.
 *e.g. "www.example.com" => "www", "example.com".
 */
function getDomainAndSubdomain(domain) {
    const parts = domain.split('.');
    if (parts.length < 2) {
        throw new Error(`No TLD found on ${domain}`);
    }
    if (parts.length === 2) {
        return { subdomain: '', parentDomain: domain };
    }

    const subdomain = parts[0];
    parts.shift();
    return {
        subdomain,
        // Trailing "." to canonicalize domain.
        parentDomain: parts.join('.') + '.',
    };
}

/**
 * Recursive crawls the provided directory, applying the provided function
 * to every file it contains. Doesn't handle cycles from symlinks.
 */
function crawlDirectory(dir, f) {
    const files = fs.readdirSync(dir);
    for (const file of files) {
        const filePath = `${dir}/${file}`;
        const stat = fs.statSync(filePath);
        if (stat.isDirectory()) {
            crawlDirectory(filePath, f);
        }
        if (stat.isFile()) {
            f(filePath);
        }
    }
}

module.exports = {
    getDomainAndSubdomain,
    crawlDirectory,
};