// @ts-check
// Note: type annotations allow type checking and IDEs autocompletion

const lightCodeTheme = require('prism-react-renderer/themes/github');
const darkCodeTheme = require('prism-react-renderer/themes/dracula');

/** @type {import('@docusaurus/types').Config} */
const config = {
  title: 'Apache Karaf Minho',
  tagline: 'NewGen modulith runtime',
  url: 'https://jbonofre.github.io/karaf5/',
  baseUrl: '/minho/',
  onBrokenLinks: 'throw',
  onBrokenMarkdownLinks: 'warn',
  favicon: 'img/favicon.ico',

  // GitHub pages deployment config.
  // If you aren't using GitHub pages, you don't need these.
  organizationName: 'jbonofre', // Usually your GitHub org/user name.
  projectName: 'karaf5', // Usually your repo name.

  // Even if you don't use internalization, you can use this field to set useful
  // metadata like html lang. For example, if your site is Chinese, you may want
  // to replace "en" with "zh-Hans".
  i18n: {
    defaultLocale: 'en',
    locales: ['en'],
  },

  presets: [
    [
      'classic',
      /** @type {import('@docusaurus/preset-classic').Options} */
      ({
        docs: {
          sidebarPath: require.resolve('./sidebars.js'),
        },
        theme: {
          customCss: require.resolve('./src/css/custom.css'),
        },
      }),
    ],
  ],

  themeConfig:
    /** @type {import('@docusaurus/preset-classic').ThemeConfig} */
    ({
      navbar: {
        logo: {
          alt: 'Apache Karaf Minho',
          src: 'img/logo.png',
        },
        items: [
          {
            type: 'doc',
            docId: 'intro',
            position: 'right',
            label: 'Documentation',
          },
          {
            label: 'Download',
            position: 'right',
            to: 'download',
          },
          {
            label: 'Creator',
            position: 'right',
            to: 'creator',
          },
					{
						label: 'Orchestrator',
						position: 'right',
						to: 'orchestrator',
					},
          {
            label: 'Examples',
            position: 'right',
            to: 'examples',
          },
          {
            href: 'https://github.com/jbonofre/karaf5',
            label: 'GitHub',
            position: 'right',
          },
        ],
      },
      footer: {
        style: 'dark',
        links: [
          {
            title: 'Community',
            items: [
              {
                label: 'User Mailing List',
                href: 'https://lists.apache.org/list.html?user@karaf.apache.org',
              },
              {
                label: 'Dev Mailing List',
                href: 'https://lists.apache.org/list.html?dev@karaf.apache.org',
              },
              {
                label: 'Slack',
                href: 'https://the-asf.slack.com/',
              },
              {
                label: 'Issues',
                href: 'https://github.com/jbonofre/karaf5/issues',
              },
            ],
          },
          {
            title: 'Project',
            items: [
              {
                label: 'GitHub',
                href: 'https://github.com/jbonofre/karaf5',
              },
              {
                label: 'CI/CD',
                href: 'https://github.com/jbonofre/karaf5/actions',
              },
              {
                label: 'Team',
                to: 'team',
              },
              {
                label: 'Contribute',
                to: 'contribute',
              },
              {
                label: 'Support',
                to: 'support',
              },
            ],
          },
          {
            title: 'Apache',
            items: [
              {
                label: 'The Apache Software Foundation',
                href: 'https://www.apache.org/',
              },
              {
                label: 'Privacy Policy',
                href: 'https://karaf.apache.org/privacy.html',
              },
              {
                label: 'Apache Events',
                href: 'https://www.apache.org/events/current-event.html',
              },
              {
                label: 'Licenses',
                href: 'https://www.apache.org/licenses/',
              },
              {
                label: 'Security',
                href: 'https://www.apache.org/security/',
              },
              {
                label: 'Sponsorship',
                href: 'https://www.apache.org/foundation/sponsorship.html',
              },
              {
                label: 'Thanks',
                href: 'https://www.apache.org/foundation/thanks.html',
              },
            ],
          },
        ],
        copyright: `Copyright © ${new Date().getFullYear()} - Apache Karaf Minho, Apache Karaf, Karaf, Apache, the Apache feather logo, and the Apache Karaf project logo are trademarks of The Apache Software Foundation.`
      },
      prism: {
        theme: lightCodeTheme,
        darkTheme: darkCodeTheme,
      },
    }),
};

module.exports = config;
