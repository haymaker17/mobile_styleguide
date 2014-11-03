//
//  NavShellVC.h
//  ConcurMobile
//
//  Created by Paul Kramer on 10/6/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>


@interface NavShellVC : UIViewController {
	UINavigationController *navC;
}
@property (strong, nonatomic) IBOutlet UINavigationController *navC;
@end
