import React from 'react';
import clsx from 'clsx';
import styles from './styles.module.css';

const FeatureList = [
  {
    title: 'Lightning fast',
    icon: <img src="img/deadline.png" width="60px" />,
    description: (
      <>
        Minho is very light and fast. Especially if you use the Minho service model,
        you can create complex modules in a minute, running super fast.
      </>
    ),
  },
  {
    title: 'Any framework in one runtime',
    icon: <img src="img/anywhere.png" width="60px" />,
    description: (
      <>
        Minho supports a large number of Java frameworks that you can mix all
        together in one or multiple runtime instances.
      </>
    ),
  },
  {
    title: 'Cloud ready',
    icon: <img src="img/cloud.png" width="60px" />,
    description: (
      <>
        Minho is cloud ready runtime, able to run on a VM, Docker, Kubernetes, ...
      </>
    ),
  },
  {
    title: 'Green and resources efficient',
    icon: <img src="img/save-the-planet.png" width="60px" />,
    description: (
        <>
            Minho is resources efficient as it allows you to gather different modules
            in different runtimes. One module is not necessary one runtime anymore.
            Better for our planet: Minho is a green.
        </>
    ),
  },
  {
    title: 'Turnkey services',
    icon: <img src="img/deal.png" width="60px" />,
    description: (
        <>
            You want to work with a database, you want to expose a REST API.
            You don't have to implement all yourself, you can use services directly
            provided by Minho.
        </>
    ),
  },
     {
       title: 'Easily create runtime packages',
       icon: <img src="img/easy-to-use.png" width="60px" />,
       description: (
           <>
               Don't worry how to create your runtime packages. Minho provides tools
               allowing you to easily create the packages, whatever you want to create
               a folder, an archive, a image.
           </>
       ),
     }
];

function Feature({icon, title, description}) {
  return (
    <div className={clsx('col col--4')}>
      <div className="text--center">
        {icon}
      </div>
      <div className="text--center padding-horiz--md">
        <h3>{title}</h3>
        <p>{description}</p>
      </div>
    </div>
  );
}

export default function HomepageFeatures() {
  return (
    <section className={styles.features}>
      <div className="container">
        <div className="row">
          {FeatureList.map((props, idx) => (
            <Feature key={idx} {...props} />
          ))}
        </div>
      </div>
    </section>
  );
}
