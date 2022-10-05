import React from 'react';
import clsx from 'clsx';
import Link from '@docusaurus/Link';
import useDocusaurusContext from '@docusaurus/useDocusaurusContext';
import Layout from '@theme/Layout';
import HomepageFeatures from '@site/src/components/HomepageFeatures';

import styles from './index.module.css';

function HomepageHeader() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <header className={clsx('hero hero--primary', styles.heroBanner)}>
      <div className="container">
        <h1 className="hero__title">
            <img src="img/logo.png" width="30%"/>
        </h1>
			<div style={{ color: 'black' }}>
				K5 is a green Java application modules runtime.<br/>
				It provides extensible modules launchers per module kind, out of the box services that any module running on K5 runtime can leverage without cost.<br/>
				K5 is GFLE (Green Fast Light Efficient) runtime.
				</div>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title="Apache Karaf 5.x"
      description="Apache Karaf 5.x, NewGen modulith runtime">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
