//
//  AyncImageView.h
//  ConcurMobile
//
//  Created by Paul Kramer on 6/12/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>


@interface AsyncImageView : UIView {
    NSURLConnection* connection;
    NSMutableData* data;
}

- (void)loadImageFromURL:(NSURL*)url;

@end


