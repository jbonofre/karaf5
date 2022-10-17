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
            <img src="img/logo.gif" width="40%"/>
        </h1>
			<div style={{ color: 'black' }}>
				Apache Karaf Minho is a green Java application modules runtime.<br/>
				It provides extensible modules launchers per module kind, out of the box services that any module running on Minho runtime can leverage without cost.<br/>
				Minho is GFLE (Green Fast Light Efficient) runtime.
				</div>
      </div>
    </header>
  );
}

export default function Home() {
  const {siteConfig} = useDocusaurusContext();
  return (
    <Layout
      title="Apache Karaf Minho"
      description="Apache Karaf Minho, NewGen modulith runtime">
      <HomepageHeader />
      <main>
        <HomepageFeatures />
      </main>
    </Layout>
  );
}
