//
//  IgniteSalesforceTripData.m
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 8/14/12.
//  Copyright (c) 2012 Concur. All rights reserved.
//

#import "IgniteSalesforceTripData.h"
#import "EntitySalesForceTrip.h"
#import "DataConstants.h"
#import "SalesForceUserManager.h"
#import "DateTimeFormatter.h"


@interface IgniteSalesforceTripData (private)
-(void) parseData:(NSData *)data;
-(void) parseTrip:(NSDictionary*)tripDict intoEntity:(EntitySalesForceTrip*)tripEntity;
-(void) saveContext;
-(NSString*) getNonEmptyString:(NSObject*)obj;
@end


@implementation IgniteSalesforceTripData

@synthesize cliqbookTripId, salesforceTripId;
@synthesize managedObjectContext=__managedObjectContext;

-(NSString *)getMsgIdKey
{
	return SALESFORCE_TRIP_DATA;
}

- (Msg*) newMsg:(NSMutableDictionary *)parameterBag
{
    self.cliqbookTripId = [parameterBag objectForKey:@"CLIQBOOK_TRIP_ID"];
    
    NSString *query = @"SELECT+Id,+OwnerId,+IsDeleted,+Name,+CreatedById,+CreatedDate,+LastModifiedById,+LastModifiedDate,+ConcurConnect__Locator__c,+ConcurConnect__Start_Date__c,+ConcurConnect__End_Date__c,+ConcurConnect__Last_Modified_Date__c+FROM+ConcurConnect__Trip__c";
    
    NSString *path = [NSMutableString stringWithFormat:@"%@/services/data/v20.0/query?q=%@", [[SalesForceUserManager sharedInstance] getInstanceUrl], query];
    
	Msg* msg = [[Msg alloc] initWithData:[self getMsgIdKey] State:@"" Position:nil MessageData:nil URI:path MessageResponder:self ParameterBag:parameterBag];
	[msg setMethod:@"GET"];
    msg.oauth2AccessToken = [[SalesForceUserManager sharedInstance] getAccessToken];
	return msg;
}

-(void) respondToXMLData:(NSData *)data withMsg:(Msg *)msg // It's JSON, not XML!
{
    if (msg.responseCode < 200 || msg.responseCode >= 300)
	{
		return; // Return instead of parsing failed responses.
	}
    
	// Find existing trips (if any)
	NSFetchRequest *fetchRequest = [[NSFetchRequest alloc] init];
    NSEntityDescription *entity = [NSEntityDescription entityForName:@"EntitySalesForceTrip" inManagedObjectContext:self.managedObjectContext];
    [fetchRequest setEntity:entity];
    
    NSError *error;
    NSArray *aFetch = [self.managedObjectContext executeFetchRequest:fetchRequest error:&error];
    
	// Delete the existing trips (if any)
    if(aFetch != nil)
	{
        for(EntitySalesForceTrip *entity in aFetch)
            [self.managedObjectContext deleteObject:entity];
	}
	
    // Parse
    [self parseData:data];
    
    // Save the changes
    [self saveContext];
}

#pragma mark - Parsing
-(void) parseData:(NSData *)data
{
    // Deserialize data into JSON
    NSError *error = nil;
    NSObject *json = [NSJSONSerialization JSONObjectWithData:data options:0 error:&error];
    
    // Handle error deserializing into JSON
    if (error != nil)
    {
        NSString *errorDomain = (error.domain == nil ? @"" : error.domain);
        NSString *localizedDescription = (error.localizedDescription == nil ? @"": error.localizedDescription);
        NSString *localizedFailureReason = (error.localizedFailureReason == nil ? @"" : error.localizedFailureReason);
        
        NSString *errorMessage = [NSString stringWithFormat:@"IgniteSalesforceTripData::respondToXMLData: Error code = %i, domain = %@, description = %@, failure reason = %@", error.code, errorDomain, localizedDescription, localizedFailureReason];
        
        [[MCLogging getInstance] log:errorMessage Level:MC_LOG_ERRO];
        return;
    }
    
    // Expecting top-level JSON to be a dictionary
    if (json != nil && [json isKindOfClass:[NSDictionary class]])
    {
        // An 'records' key should be in the dictionary
        NSDictionary *items = (NSDictionary*)json;
        if (items != nil && [items isKindOfClass:[NSDictionary class]])
        {
            // The value of the 'records' key is an array of trip records.
            NSArray *trips = [items objectForKey:@"records"];
            
            if (trips != nil && [trips isKindOfClass:[NSArray class]])
            {
                // Go through each trip records in the array of trip records
                for (NSDictionary *trip in trips)
                {
                    EntitySalesForceTrip *tripEntity = [NSEntityDescription insertNewObjectForEntityForName:@"EntitySalesForceTrip" inManagedObjectContext:self.managedObjectContext];
                    [self parseTrip:trip intoEntity:tripEntity];
                    
                    // If we find a trip with a locator that matches the cliqbook trip id,
                    // then remember grab the salesforce id of the trip
                    NSString *tripLocator = [trip objectForKey:@"ConcurConnect__Locator__c"];
                    if (tripLocator != nil && [self.cliqbookTripId isEqualToString:tripLocator])
                    {
                        self.salesforceTripId = [trip objectForKey:@"Id"];
                    }
                }
            }
            else
            {
                [[MCLogging getInstance] log:@"IgniteSalesforceTripData::parseData: Expected array of trips" Level:MC_LOG_ERRO];
            }
       }
        else
        {
            [[MCLogging getInstance] log:@"IgniteSalesforceTripData::parseData: Expected dictionary of items" Level:MC_LOG_ERRO];
        }
    }
    else
    {
        [[MCLogging getInstance] log:@"IgniteSalesforceTripData::parseData: Expected dictionary" Level:MC_LOG_ERRO];
    }
}

-(void) parseTrip:(NSDictionary*)tripDict intoEntity:(EntitySalesForceTrip*)tripEntity;
{
    tripEntity.identifier = [self getNonEmptyString:[tripDict objectForKey:@"Id"]];
    tripEntity.locator = [self getNonEmptyString:[tripDict objectForKey:@"ConcurConnect__Locator__c"]];
    tripEntity.name = [self getNonEmptyString:[tripDict objectForKey:@"Name"]];
    
    NSString* strStartDate = [self getNonEmptyString:[tripDict objectForKey:@"ConcurConnect__Start_Date__c"]];
    tripEntity.startDate = [DateTimeFormatter getISO8601ZoneDate:strStartDate];

    NSString* strEndDate = [self getNonEmptyString:[tripDict objectForKey:@"ConcurConnect__End_Date__c"]];
    tripEntity.endDate = [DateTimeFormatter getISO8601ZoneDate:strEndDate];
}

-(NSString*) getNonEmptyString:(NSObject*)obj
{
    return ((obj != nil && [obj isKindOfClass:[NSString class]]) ? (NSString*)obj : @"");
}

#pragma mark - Context
- (NSManagedObjectContext *)managedObjectContext
{
    if (__managedObjectContext != nil)
    {
        return __managedObjectContext;
    }
    
    ConcurMobileAppDelegate *ad = (ConcurMobileAppDelegate*) [[UIApplication sharedApplication] delegate];
    NSPersistentStoreCoordinator *coordinator = [ad persistentStoreCoordinator];
    if (coordinator != nil)
    {
        __managedObjectContext = [[NSManagedObjectContext alloc] init];
        [__managedObjectContext setPersistentStoreCoordinator:coordinator];
    }
    return __managedObjectContext;
}

- (void)saveContext
{
    NSError *error = nil;
    NSManagedObjectContext *managedObjectContext = __managedObjectContext;
    if (managedObjectContext != nil)
    {
        if ([managedObjectContext hasChanges] && ![managedObjectContext save:&error])
        {
            /*
             Replace this implementation with code to handle the error appropriately.
             
             abort() causes the application to generate a crash log and terminate. You should not use this function in a shipping application, although it may be useful during development. If it is not possible to recover from the error, display an alert panel that instructs the user to quit the application by pressing the Home button.
             */
            NSLog(@"Unresolved error %@, %@", error, [error userInfo]);
            abort();
        } 
    }
}


@end
