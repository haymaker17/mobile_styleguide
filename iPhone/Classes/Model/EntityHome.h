//
//  EntityHome.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/18/11.
//  Copyright (c) 2011 Concur. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <CoreData/CoreData.h>


@interface EntityHome : NSManagedObject {
@private
}
@property (nonatomic, strong) NSString * name;
@property (nonatomic, strong) NSString * subLine;
@property (nonatomic, strong) NSDate * endDate;
@property (nonatomic, strong) NSDate * startDate;
@property (nonatomic, strong) NSNumber * rowPosition;
@property (nonatomic, strong) NSString * keyValue;
@property (nonatomic, strong) NSString * sectionValue;
@property (nonatomic, strong) NSNumber * sectionPosition;
@property (nonatomic, strong) NSString * imageName;
@property (nonatomic, strong) NSString * key;
@property (nonatomic, strong) NSNumber * itemCount;


@end
